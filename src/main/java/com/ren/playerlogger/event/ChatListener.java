package com.ren.playerlogger.event;

import com.ren.playerlogger.PlayerLogger;
import com.ren.playerlogger.config.ConfigManager;
import com.ren.playerlogger.log.LogManager;
import com.ren.playerlogger.log.events.PlayerEvent;

import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;

import java.util.UUID;

// 聊天监听器类
public class ChatListener {
    private final LogManager logManager;
    private final ConfigManager configManager;

    public ChatListener(LogManager logManager, ConfigManager configManager) {
        this.logManager = logManager;
        this.configManager = configManager;
    }

    // 处理聊天消息
    public ActionResult onChatMessage(ServerPlayerEntity player, SignedMessage message) {
        if (configManager.isEnabled() && configManager.shouldLogChat()) {
            try {
                // 获取消息内容
                String messageContent = message.getContent().getString();

                // 记录聊天事件
                logManager.logEvent(new PlayerEvent.Chat(
                        player,
                        messageContent
                ));

                // 返回PASS表示不拦截消息
                return ActionResult.PASS;
            } catch (Exception e) {
                PlayerLogger.LOGGER.error("处理聊天消息时出错", e);
            }
        }

        return ActionResult.PASS;
    }

    // 处理命令消息
    public ActionResult onCommandMessage(ServerPlayerEntity player, String command) {
        if (configManager.isEnabled() && configManager.shouldLogCommands()) {
            try {
                // 记录命令事件
                logManager.logEvent(new PlayerEvent.Chat(
                        player,
                        "/" + command
                ));

                // 返回PASS表示不拦截命令
                return ActionResult.PASS;
            } catch (Exception e) {
                PlayerLogger.LOGGER.error("处理命令消息时出错", e);
            }
        }

        return ActionResult.PASS;
    }
}