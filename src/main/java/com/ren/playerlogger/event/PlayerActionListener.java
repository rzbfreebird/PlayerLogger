package com.ren.playerlogger.event;

import com.ren.playerlogger.PlayerLogger;
import com.ren.playerlogger.config.ConfigManager;
import com.ren.playerlogger.log.LogManager;
import com.ren.playerlogger.log.events.PlayerEvent;

import net.minecraft.block.BlockState;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerActionListener {
    private final LogManager logManager;
    private final ConfigManager configManager;

    // 记录上次操作时间，避免短时间内重复记录
    private final Map<UUID, Map<String, Long>> lastActionTimes = new HashMap<>();

    // 操作冷却时间
    private static final long ACTION_COOLDOWN_MS = 500;

    public PlayerActionListener(LogManager logManager, ConfigManager configManager) {
        this.logManager = logManager;
        this.configManager = configManager;
    }

    // 处理方块交互事件（用于Mixin回调）
    public void onBlockInteract(ServerPlayerEntity player, BlockPos pos, BlockState state) {
        if (!configManager.isEnabled() || !configManager.shouldLogBlockInteraction()) {
            return;
        }

        try {
            String blockName = state.getBlock().getName().getString();

            // 防止短时间内重复记录
            UUID playerId = player.getUuid();
            String actionKey = "blockinteract:" + pos.toString();
            if (isOnCooldown(playerId, actionKey)) {
                return;
            }

            // 记录方块交互事件，添加"使用"作为动作类型
            logManager.logEvent(new PlayerEvent.BlockInteract(
                    player,
                    pos,
                    blockName,
                    "使用"  // 添加缺少的action参数
            ));

            // 更新最后操作时间
            updateLastActionTime(playerId, actionKey);
        } catch (Exception e) {
            PlayerLogger.LOGGER.error("记录方块交互事件时出错", e);
        }
    }

    // 检查是否在冷却时间内
    private boolean isOnCooldown(UUID playerId, String actionKey) {
        Map<String, Long> playerActions = lastActionTimes.get(playerId);
        if (playerActions == null) {
            return false;
        }

        Long lastTime = playerActions.get(actionKey);
        return lastTime != null && (System.currentTimeMillis() - lastTime) < ACTION_COOLDOWN_MS;
    }

    // 更新最后操作时间
    private void updateLastActionTime(UUID playerId, String actionKey) {
        Map<String, Long> playerActions = lastActionTimes.computeIfAbsent(
                playerId, k -> new HashMap<>());
        playerActions.put(actionKey, System.currentTimeMillis());
    }

    // 清理玩家数据
    public void clearPlayerData(UUID playerId) {
        lastActionTimes.remove(playerId);
    }
}