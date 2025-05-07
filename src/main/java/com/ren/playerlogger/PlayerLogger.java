package com.ren.playerlogger;

import com.ren.playerlogger.config.ConfigManager;
import com.ren.playerlogger.event.BlockInteractionListener;
import com.ren.playerlogger.event.ChatListener;
import com.ren.playerlogger.event.EntityInteractionListener;
import com.ren.playerlogger.event.EventManager;
import com.ren.playerlogger.event.InventoryListener;
import com.ren.playerlogger.event.PlayerActionListener;
import com.ren.playerlogger.log.LogManager;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlayerLogger implements ModInitializer {
	// 日志记录器
	public static final Logger LOGGER = LoggerFactory.getLogger("playerlogger");

	// 组件实例
	private static ConfigManager configManager;
	private static LogManager logManager;
	private static EventManager eventManager;
	private static ChatListener chatListener;
	private static InventoryListener inventoryListener;
	private static BlockInteractionListener blockInteractionListener;
	private static EntityInteractionListener entityInteractionListener;
	private static PlayerActionListener playerActionListener;

	@Override
	public void onInitialize() {
		LOGGER.info("正在初始化 PlayerLogger...");

		try {
			// 初始化配置管理器
			configManager = new ConfigManager();

			// 初始化日志管理器
			logManager = new LogManager(configManager);

			// 初始化事件管理器
			eventManager = new EventManager(logManager, configManager);

			// 初始化聊天监听器
			chatListener = new ChatListener(logManager, configManager);

			// 初始化容器监听器
			inventoryListener = new InventoryListener(logManager, configManager);

			// 初始化方块交互监听器
			blockInteractionListener = new BlockInteractionListener(logManager, configManager);

			// 初始化实体交互监听器
			entityInteractionListener = new EntityInteractionListener(logManager, configManager);

			// 初始化玩家动作监听器
			playerActionListener = new PlayerActionListener(logManager, configManager);

			// 注册服务器关闭事件
			ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
				LOGGER.info("正在关闭 PlayerLogger...");

				// 关闭事件管理器
				if (eventManager != null) {
					eventManager.close();
				}

				// 关闭日志管理器
				if (logManager != null) {
					logManager.close();
				}
			});

			LOGGER.info("PlayerLogger 初始化完成");
		} catch (Exception e) {
			LOGGER.error("初始化 PlayerLogger 时出错", e);
		}
	}

	// 获取配置管理器
	public static ConfigManager getConfigManager() {
		return configManager;
	}

	// 获取日志管理器
	public static LogManager getLogManager() {
		return logManager;
	}

	// 获取事件管理器
	public static EventManager getEventManager() {
		return eventManager;
	}

	// 获取容器监听器
	public static InventoryListener getInventoryListener() {
		return inventoryListener;
	}

	// 获取方块交互监听器
	public static BlockInteractionListener getBlockInteractionListener() {
		return blockInteractionListener;
	}

	// 获取实体交互监听器
	public static EntityInteractionListener getEntityInteractionListener() {
		return entityInteractionListener;
	}

	// 获取玩家动作监听器
	public static PlayerActionListener getPlayerActionListener() {
		return playerActionListener;
	}
}