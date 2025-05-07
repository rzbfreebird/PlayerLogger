package com.ren.playerlogger.event;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;

import com.ren.playerlogger.config.ConfigManager;
import com.ren.playerlogger.log.LogManager;
import com.ren.playerlogger.log.events.PlayerEvent;

public class PlayerConnectionListener {
    private final LogManager logManager;
    private final ConfigManager configManager;

    public PlayerConnectionListener(LogManager logManager, ConfigManager configManager) {
        this.logManager = logManager;
        this.configManager = configManager;
    }

    public void register() {
        // 监听玩家加入服务器
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            if (configManager.isPlayerLoginEnabled()) {
                ServerPlayerEntity player = handler.getPlayer();
                logManager.logEvent(new PlayerEvent.Login(player));
            }
        });

        // 监听玩家离开服务器
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            if (configManager.isPlayerLogoutEnabled()) {
                ServerPlayerEntity player = handler.getPlayer();
                logManager.logEvent(new PlayerEvent.Logout(player));
            }
        });
    }
}