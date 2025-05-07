package com.ren.playerlogger.event;

import com.ren.playerlogger.PlayerLogger;
import com.ren.playerlogger.config.ConfigManager;
import com.ren.playerlogger.log.LogManager;
import com.ren.playerlogger.log.events.PlayerEvent;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InventoryListener {
    private final LogManager logManager;
    private final ConfigManager configManager;

    private final Map<UUID, Map<BlockPos, Long>> lastContainerAccessTimes = new ConcurrentHashMap<>();

    private static final long COOLDOWN_MS = 2000;

    public InventoryListener(LogManager logManager, ConfigManager configManager) {
        this.logManager = logManager;
        this.configManager = configManager;
    }

    public void onOpenContainer(ServerPlayerEntity player, BlockEntity blockEntity) {
        if (!configManager.isEnabled() || !configManager.shouldLogContainerAccess()) {
            return;
        }

        try {
            if (blockEntity instanceof NamedScreenHandlerFactory) {
                BlockPos pos = blockEntity.getPos();
                String containerName = "";

                if (blockEntity instanceof NamedScreenHandlerFactory) {
                    containerName = ((NamedScreenHandlerFactory) blockEntity).getDisplayName().getString();
                } else if (blockEntity instanceof Inventory) {
                    containerName = "容器";
                } else {
                    containerName = blockEntity.getClass().getSimpleName();
                }

                if (isOnCooldown(player.getUuid(), pos)) {
                    return;
                }

                logManager.logEvent(new PlayerEvent.ContainerAccess(
                        player,
                        containerName,
                        pos
                ));

                updateLastAccessTime(player.getUuid(), pos);
            }
        } catch (Exception e) {
            PlayerLogger.LOGGER.error("记录容器访问事件时出错", e);
        }
    }

    private boolean isOnCooldown(UUID playerId, BlockPos pos) {
        Map<BlockPos, Long> playerMap = lastContainerAccessTimes.get(playerId);
        if (playerMap == null) {
            return false;
        }

        Long lastTime = playerMap.get(pos);
        return lastTime != null && (System.currentTimeMillis() - lastTime) < COOLDOWN_MS;
    }

    private void updateLastAccessTime(UUID playerId, BlockPos pos) {
        Map<BlockPos, Long> playerMap = lastContainerAccessTimes.computeIfAbsent(
                playerId, k -> new ConcurrentHashMap<>());
        playerMap.put(pos, System.currentTimeMillis());
    }

    public void clearPlayerData(UUID playerId) {
        lastContainerAccessTimes.remove(playerId);
    }

    public void close() {
        lastContainerAccessTimes.clear();
    }
}