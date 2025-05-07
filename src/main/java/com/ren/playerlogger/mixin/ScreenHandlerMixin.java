package com.ren.playerlogger.mixin;

import com.ren.playerlogger.PlayerLogger;
import com.ren.playerlogger.log.events.PlayerEvent;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Mixin(ScreenHandler.class)
public class ScreenHandlerMixin {
    // 物品移动记录冷却缓存
    private static final Map<String, Long> RECENT_MOVES = new HashMap<>();
    // 冷却时间（毫秒）
    private static final long MOVE_COOLDOWN_MS = 300;

    @Inject(at = @At("HEAD"), method = "onSlotClick")
    private void onSlotClick(int slotIndex, int button, SlotActionType actionType,
                             PlayerEntity player, CallbackInfo ci) {
        // 只在服务器端处理且玩家是ServerPlayerEntity时执行逻辑
        if (!player.getWorld().isClient && player instanceof ServerPlayerEntity) {
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity)player;
            ScreenHandler screenHandler = (ScreenHandler)(Object)this;

            // 确保槽位索引有效
            if (slotIndex >= 0 && slotIndex < screenHandler.slots.size()) {
                Slot slot = screenHandler.slots.get(slotIndex);
                ItemStack stack = slot.getStack();

                if (!stack.isEmpty()) {
                    // 检查是否是从容器到玩家物品栏的操作
                    boolean isPlayerInventory = slot.inventory instanceof PlayerInventory;

                    // 获取物品和容器信息
                    String itemName = stack.getName().getString();
                    int count = stack.getCount();
                    String action = getActionName(actionType, button);

                    // 获取容器名称和位置
                    Inventory sourceInventory = slot.inventory;
                    String containerName = getContainerName(sourceInventory, screenHandler);
                    BlockPos containerPos = getContainerPosition(sourceInventory);

                    // 冷却检查，避免重复记录
                    long currentTime = System.currentTimeMillis();
                    String moveKey = serverPlayer.getUuid() + "-" + itemName + "-" + slotIndex + "-" + action;
                    Long lastMoveTime = RECENT_MOVES.get(moveKey);

                    if (lastMoveTime != null && (currentTime - lastMoveTime) < MOVE_COOLDOWN_MS) {
                        return; // 在冷却期内，不记录
                    }

                    // 判断是容器到玩家物品栏还是整理
                    boolean shouldLog = false;

                    // 根据操作类型判断是否记录
                    switch (actionType) {
                        case PICKUP: // 左键或右键点击
                            // 只记录从容器拿取到玩家手上的操作
                            shouldLog = !isPlayerInventory && button == 0;
                            break;
                        case QUICK_MOVE: // Shift+点击
                            // 只记录从容器快速移动到玩家物品栏的操作
                            shouldLog = !isPlayerInventory;
                            break;
                        case SWAP: // 热键交换
                        case THROW: // 丢弃
                            // 通常不记录这些操作，主要记录拿取操作
                            break;
                        default:
                            // 不记录其他操作类型
                            break;
                    }

                    // 记录物品移动事件
                    if (shouldLog && PlayerLogger.getConfigManager().shouldLogContainerAccess()) {
                        // 更新冷却时间
                        RECENT_MOVES.put(moveKey, currentTime);

                        // 清理过期记录，防止内存泄漏
                        if (RECENT_MOVES.size() > 1000) {
                            RECENT_MOVES.entrySet().removeIf(entry ->
                                    (currentTime - entry.getValue()) > 10000);
                        }

                        PlayerLogger.getEventManager().processItemMoveEvent(
                                new PlayerEvent.ItemMove(serverPlayer, containerName, itemName, count, action, containerPos)
                        );
                    }
                }
            }
        }
    }

    // 根据操作类型获取动作名称
    private String getActionName(SlotActionType actionType, int button) {
        switch (actionType) {
            case PICKUP:
                return button == 0 ? "拿取" : "分割";
            case QUICK_MOVE:
                return "快速拿取";
            case SWAP:
                return "交换";
            case THROW:
                return "丢弃";
            default:
                return "移动";
        }
    }

    // 获取容器名称
    private String getContainerName(Inventory inventory, ScreenHandler handler) {
        // 尝试从BlockEntity获取更友好的名称
        if (inventory instanceof BlockEntity) {
            BlockEntity blockEntity = (BlockEntity)inventory;
            String blockName = blockEntity.getCachedState().getBlock().getName().getString();
            if (blockName != null && !blockName.startsWith("block.minecraft.")) {
                return blockName;
            }
        }

        // 如果无法从BlockEntity获取，则使用ScreenHandler类名
        String handlerName = handler.getClass().getSimpleName();
        if (handlerName.endsWith("ScreenHandler")) {
            handlerName = handlerName.substring(0, handlerName.length() - "ScreenHandler".length());
        }

        return handlerName;
    }

    // 获取容器位置
    private BlockPos getContainerPosition(Inventory inventory) {
        if (inventory instanceof BlockEntity) {
            return ((BlockEntity)inventory).getPos();
        }
        return null;
    }
}