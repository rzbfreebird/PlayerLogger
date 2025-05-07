package com.ren.playerlogger.event;

import com.ren.playerlogger.PlayerLogger;
import com.ren.playerlogger.config.ConfigManager;
import com.ren.playerlogger.log.LogManager;
import com.ren.playerlogger.log.events.PlayerEvent;
import com.ren.playerlogger.log.events.LogEvent;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class EventManager {
    private final LogManager logManager;
    private final ConfigManager configManager;

    // 已注册的事件处理器列表
    private final List<Object> registeredHandlers = new ArrayList<>();

    // 物品拾取事件时间记录
    private final Map<UUID, Long> lastItemPickupTimes = new ConcurrentHashMap<>();

    // 交互事件时间记录
    private final Map<UUID, Map<String, Long>> lastInteractionTimes = new ConcurrentHashMap<>();

    // 事件处理器注册状态标记
    private boolean eventHandlersRegistered = false;

    // 交互前方块状态记录
    private final Map<BlockPos, BlockState> blockStateBeforeInteraction = new HashMap<>();

    // 缓存每个玩家的物品栏状态
    private final Map<UUID, Map<Integer, ItemStack>> lastPlayerInventories = new ConcurrentHashMap<>();

    public EventManager(LogManager logManager, ConfigManager configManager) {
        this.logManager = logManager;
        this.configManager = configManager;

        if (configManager.isEnabled()) {
            registerEventHandlers();
        }
    }

    private void registerEventHandlers() {
        // 防止重复注册事件处理器
        if (eventHandlersRegistered) {
            PlayerLogger.LOGGER.info("事件处理器已经注册，跳过重复注册");
            return;
        }

        try {
            // 玩家登录事件
            if (configManager.isPlayerLoginEnabled()) {
                ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
                    ServerPlayerEntity player = handler.getPlayer();
                    logManager.logEvent(new PlayerEvent.Login(player));

                    // 初始化物品栏缓存
                    Map<Integer, ItemStack> inventory = new HashMap<>();
                    for (int slot = 0; slot < player.getInventory().size(); slot++) {
                        ItemStack stack = player.getInventory().getStack(slot);
                        if (!stack.isEmpty()) {
                            inventory.put(slot, stack.copy());
                        }
                    }
                    lastPlayerInventories.put(player.getUuid(), inventory);
                });
                registeredHandlers.add(ServerPlayConnectionEvents.JOIN);
            }

            // 玩家登出事件
            if (configManager.isPlayerLogoutEnabled()) {
                ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
                    ServerPlayerEntity player = handler.getPlayer();
                    logManager.logEvent(new PlayerEvent.Logout(player));

                    // 清理物品栏缓存
                    lastPlayerInventories.remove(player.getUuid());
                });
                registeredHandlers.add(ServerPlayConnectionEvents.DISCONNECT);
            }

            // 方块破坏事件
            if (configManager.shouldLogBlockBreak()) {
                PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
                    if (player instanceof ServerPlayerEntity) {
                        ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
                        // 使用Text.translatable替代过时的getName().getString()
                        String blockName = Text.translatable(state.getBlock().getTranslationKey()).getString();

                        // 防止重复记录
                        UUID playerId = player.getUuid();
                        String actionKey = "blockbreak:" + pos.toString();
                        if (!shouldLogInteraction(playerId, actionKey)) {
                            return;
                        }

                        logManager.logEvent(
                                new PlayerEvent.BlockBreak(serverPlayer, pos, blockName)
                        );
                    }
                });
            }

            // 方块交互和放置事件
            if (configManager.shouldLogBlockInteraction()) {
                // 记录交互前的方块状态
                UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
                    if (!world.isClient() && player instanceof ServerPlayerEntity) {
                        BlockPos pos = hitResult.getBlockPos();
                        blockStateBeforeInteraction.put(pos, world.getBlockState(pos));
                    }
                    return ActionResult.PASS;
                });

                // 服务器tick结束时检查方块状态变化
                ServerTickEvents.END_SERVER_TICK.register(server -> {
                    if (!blockStateBeforeInteraction.isEmpty()) {
                        // 复制状态记录防止并发修改
                        Map<BlockPos, BlockState> beforeStates = new HashMap<>(blockStateBeforeInteraction);
                        blockStateBeforeInteraction.clear();

                        // 遍历检查所有记录的方块状态
                        for (Map.Entry<BlockPos, BlockState> entry : beforeStates.entrySet()) {
                            BlockPos pos = entry.getKey();
                            BlockState beforeState = entry.getValue();
                            World world = server.getOverworld();

                            if (world.isChunkLoaded(pos)) {
                                BlockState afterState = world.getBlockState(pos);

                                // 方块状态发生了变化，可能是放置导致的
                                if (!afterState.equals(beforeState)) {
                                    // 查找附近的玩家作为放置者
                                    server.getPlayerManager().getPlayerList().forEach(player -> {
                                        if (player.getBlockPos().isWithinDistance(pos, 5)) {
                                            UUID playerId = player.getUuid();
                                            String actionKey = "blockplace:" + pos.toString();
                                            if (shouldLogInteraction(playerId, actionKey)) {
                                                // 记录方块放置事件
                                                String blockName = Text.translatable(afterState.getBlock().getTranslationKey()).getString();
                                                logManager.logEvent(
                                                        new PlayerEvent.BlockPlace(player, pos, blockName)
                                                );
                                            }
                                        }
                                    });
                                }
                            }
                        }
                    }
                });
            }

            // 物品使用事件
            if (configManager.shouldLogItemUse()) {
                UseItemCallback.EVENT.register((player, world, hand) -> {
                    if (!world.isClient() && player instanceof ServerPlayerEntity) {
                        ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
                        ItemStack heldItem = player.getStackInHand(hand);

                        if (!heldItem.isEmpty()) {
                            // 使用Text.translatable替代过时的getName().getString()
                            String itemName = Text.translatable(heldItem.getItem().getTranslationKey()).getString();

                            // 防止重复记录
                            UUID playerId = player.getUuid();
                            String actionKey = "itemuse:" + itemName;
                            if (!shouldLogInteraction(playerId, actionKey)) {
                                return TypedActionResult.pass(player.getStackInHand(hand));
                            }

                            // 记录使用前的物品状态
                            ItemStack checkStack = heldItem.copy();
                            int oldCount = checkStack.getCount();

                            // 在下一tick检查物品状态变化
                            if (world.getServer() != null) {
                                world.getServer().execute(() -> {
                                    ItemStack newStack = player.getStackInHand(hand);
                                    // 如果物品数量减少或物品变化，则为有效使用
                                    if (newStack.isEmpty() || newStack.getCount() < oldCount || !ItemStack.areEqual(newStack, checkStack)) {
                                        logManager.logEvent(
                                                new PlayerEvent.ItemUse(serverPlayer, itemName)
                                        );
                                    }
                                });
                            }
                        }
                    }
                    return TypedActionResult.pass(player.getStackInHand(hand));
                });
            }

            // 物品拾取事件
            if (configManager.shouldLogItemPickup()) {
                // 使用服务器tick事件监听物品栏变化而不是物品栏监听器
                ServerTickEvents.END_SERVER_TICK.register(server -> {
                    for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                        // 检查物品栏变化
                        for (int slot = 0; slot < player.getInventory().size(); slot++) {
                            ItemStack currentStack = player.getInventory().getStack(slot);
                            UUID playerId = player.getUuid();

                            Map<Integer, ItemStack> lastInventory = lastPlayerInventories.computeIfAbsent(
                                    playerId, k -> new HashMap<>());
                            ItemStack lastStack = lastInventory.get(slot);

                            // 检测新增物品或数量增加
                            if ((lastStack == null || lastStack.isEmpty()) && !currentStack.isEmpty()) {
                                // 新物品
                                // 使用Text.translatable替代过时的getName().getString()
                                String itemName = Text.translatable(currentStack.getItem().getTranslationKey()).getString();
                                int count = currentStack.getCount();

                                // 防止重复记录
                                String actionKey = "itempickup:" + itemName;
                                if (shouldLogInteraction(playerId, actionKey)) {
                                    logManager.logEvent(
                                            new PlayerEvent.ItemPickup(player, itemName, count)
                                    );
                                }
                            } else if (lastStack != null && !lastStack.isEmpty() && !currentStack.isEmpty() &&
                                    lastStack.getItem() == currentStack.getItem() &&
                                    currentStack.getCount() > lastStack.getCount()) {
                                // 物品数量增加
                                // 使用Text.translatable替代过时的getName().getString()
                                String itemName = Text.translatable(currentStack.getItem().getTranslationKey()).getString();
                                int countDiff = currentStack.getCount() - lastStack.getCount();

                                // 防止重复记录
                                String actionKey = "itempickup:" + itemName;
                                if (shouldLogInteraction(playerId, actionKey)) {
                                    logManager.logEvent(
                                            new PlayerEvent.ItemPickup(player, itemName, countDiff)
                                    );
                                }
                            }

                            // 更新缓存
                            if (currentStack.isEmpty()) {
                                lastInventory.remove(slot);
                            } else {
                                lastInventory.put(slot, currentStack.copy());
                            }
                        }
                    }
                });
            }

            // 物品丢弃事件
            if (configManager.shouldLogItemDrop()) {
                // 监听物品实体生成
                ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
                    if (!world.isClient && entity instanceof ItemEntity) {
                        ItemEntity itemEntity = (ItemEntity) entity;

                        // 检查物品是新生成的且有所有者
                        Entity owner = itemEntity.getOwner();
                        if (owner instanceof ServerPlayerEntity && itemEntity.age < 5) {
                            ServerPlayerEntity player = (ServerPlayerEntity) owner;
                            // 使用Text.translatable替代过时的getName().getString()
                            String itemName = Text.translatable(itemEntity.getStack().getItem().getTranslationKey()).getString();
                            int count = itemEntity.getStack().getCount();

                            // 防止重复记录
                            UUID playerId = player.getUuid();
                            String actionKey = "itemdrop:" + itemName;
                            if (shouldLogInteraction(playerId, actionKey)) {
                                // 记录物品丢弃事件
                                logManager.logEvent(
                                        new PlayerEvent.ItemDrop(player, itemName, count)
                                );
                            }
                        }
                    }
                });
            }

            // 实体交互事件
            if (configManager.shouldLogEntityInteraction()) {
                AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
                    if (!world.isClient() && player instanceof ServerPlayerEntity) {
                        ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
                        // 使用Text.translatable替代过时的getName().getString()
                        String entityName = Text.translatable(entity.getType().getTranslationKey()).getString();
                        String actionType = "攻击";
                        String position = entity.getBlockPos().toString();

                        // 防止重复记录
                        UUID playerId = player.getUuid();
                        String actionKey = "entityinteract:" + entityName;
                        if (shouldLogInteraction(playerId, actionKey)) {
                            logManager.logEvent(
                                    new PlayerEvent.EntityInteract(serverPlayer, entityName, actionType, position)
                            );
                        }
                    }
                    return ActionResult.PASS;
                });
            }

            // 标记事件处理器已注册完成
            eventHandlersRegistered = true;
            PlayerLogger.LOGGER.info("成功注册所有事件处理器");

        } catch (Exception e) {
            PlayerLogger.LOGGER.error("注册事件处理器失败", e);
        }
    }

    // 判断是否应该记录交互（防止短时间内重复记录）
    private boolean shouldLogInteraction(UUID playerId, String actionKey) {
        long now = System.currentTimeMillis();

        // 获取玩家的交互记录
        Map<String, Long> playerInteractions = lastInteractionTimes.computeIfAbsent(playerId, k -> new HashMap<>());

        // 判断是否在短时间内（500毫秒）有相同交互
        Long lastTime = playerInteractions.get(actionKey);
        if (lastTime != null && (now - lastTime) < 500) {
            return false;
        }

        // 记录本次交互时间
        playerInteractions.put(actionKey, now);
        return true;
    }

    // 处理来自Mixin的事件
    public void processEvent(LogEvent event) {
        if (configManager.isEnabled()) {
            logManager.logEvent(event);
        }
    }

    // 处理物品移动事件
    public void processItemMoveEvent(PlayerEvent.ItemMove event) {
        if (configManager.isEnabled() && configManager.shouldLogContainerAccess()) {
            logManager.logEvent(event);
        }
    }

    // 关闭事件管理器
    public void close() {
        try {
            PlayerLogger.LOGGER.info("关闭事件管理器...");

            // 清理所有记录和监听器
            registeredHandlers.clear();
            lastItemPickupTimes.clear();
            lastInteractionTimes.clear();
            blockStateBeforeInteraction.clear();
            lastPlayerInventories.clear();

            PlayerLogger.LOGGER.info("事件管理器已关闭");
        } catch (Exception e) {
            PlayerLogger.LOGGER.error("关闭事件管理器时出错", e);
        }
    }
}