package com.ren.playerlogger.event;

import com.ren.playerlogger.PlayerLogger;
import com.ren.playerlogger.config.ConfigManager;
import com.ren.playerlogger.log.LogManager;
import com.ren.playerlogger.log.events.PlayerEvent;

import net.minecraft.block.BlockState;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BlockInteractionListener {
    private final LogManager logManager;
    private final ConfigManager configManager;

    // 记录玩家上一次交互信息，防止短时间内重复记录
    private final Map<UUID, Map<String, Long>> lastInteractionTimes = new HashMap<>();

    // 交互冷却时间（毫秒）
    private static final long INTERACTION_COOLDOWN_MS = 500;

    public BlockInteractionListener(LogManager logManager, ConfigManager configManager) {
        this.logManager = logManager;
        this.configManager = configManager;
    }

    // 处理方块交互事件
    public ActionResult onInteractBlock(ServerPlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
        if (configManager.isEnabled() && configManager.shouldLogBlockInteraction()) {
            try {
                BlockPos pos = hitResult.getBlockPos();
                BlockState state = world.getBlockState(pos);
                String blockName = state.getBlock().getName().getString();
                String action = "交互";

                // 防止短时间内重复记录
                UUID playerId = player.getUuid();
                String actionKey = "blockinteract:" + pos.toString();
                if (isOnCooldown(playerId, actionKey)) {
                    return ActionResult.PASS;
                }

                // 记录方块交互事件
                logManager.logEvent(new PlayerEvent.BlockInteract(
                        player,
                        pos,
                        blockName,
                        action
                ));

                // 更新最后交互时间
                updateLastInteractionTime(playerId, actionKey);
            } catch (Exception e) {
                PlayerLogger.LOGGER.error("记录方块交互事件时出错", e);
            }
        }

        return ActionResult.PASS;
    }

    // 检查是否在冷却时间内
    private boolean isOnCooldown(UUID playerId, String actionKey) {
        Map<String, Long> playerInteractions = lastInteractionTimes.get(playerId);
        if (playerInteractions == null) {
            return false;
        }

        Long lastTime = playerInteractions.get(actionKey);
        return lastTime != null && (System.currentTimeMillis() - lastTime) < INTERACTION_COOLDOWN_MS;
    }

    // 更新最后交互时间
    private void updateLastInteractionTime(UUID playerId, String actionKey) {
        Map<String, Long> playerInteractions = lastInteractionTimes.computeIfAbsent(
                playerId, k -> new HashMap<>());
        playerInteractions.put(actionKey, System.currentTimeMillis());
    }

    // 清理玩家数据
    public void clearPlayerData(UUID playerId) {
        lastInteractionTimes.remove(playerId);
    }
}