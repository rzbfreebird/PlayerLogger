package com.ren.playerlogger.log;

import com.ren.playerlogger.PlayerLogger;
import com.ren.playerlogger.config.ConfigManager;
import com.ren.playerlogger.log.events.LogEvent;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogManager {
    private final ConfigManager configManager;
    private BufferedWriter logWriter;
    private final SimpleDateFormat dateFormat;
    private File logFile;

    public LogManager(ConfigManager configManager) {
        this.configManager = configManager;
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        if (configManager.isEnabled()) {
            initLogFile();
        }
    }

    private void initLogFile() {
        try {
            // 确保日志目录存在
            File logDir = new File("logs/playerlogger");
            if (!logDir.exists() && !logDir.mkdirs()) {
                PlayerLogger.LOGGER.error("无法创建日志目录");
                return;
            }

            // 使用当前日期创建日志文件
            SimpleDateFormat fileDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String fileName = fileDateFormat.format(new Date()) + ".log";
            logFile = new File(logDir, fileName);

            // 如果文件不存在，创建它
            boolean newFile = !logFile.exists();
            logWriter = new BufferedWriter(new FileWriter(logFile, true));

            // 如果是新文件，写入标题
            if (newFile) {
                PlayerLogger.LOGGER.info("创建新日志文件: " + logFile.getAbsolutePath());
            } else {
                PlayerLogger.LOGGER.info("追加到现有日志文件: " + logFile.getAbsolutePath());
            }
        } catch (IOException e) {
            PlayerLogger.LOGGER.error("初始化日志文件时出错", e);
        }
    }

    public void logEvent(LogEvent event) {
        if (logWriter == null || !configManager.isEnabled()) {
            return;
        }

        try {
            // 处理日志格式
            String logFormat = configManager.getLogFormat();
            String timestamp = dateFormat.format(new Date());
            String playerName = event.getPlayerName();
            String action = event.getDescription();

            // 替换格式中的占位符
            String logMessage = logFormat
                    .replace("%date%", timestamp)
                    .replace("%player%", playerName)
                    .replace("%action%", action);

            // 写入日志
            logWriter.write(logMessage);
            logWriter.newLine();
            logWriter.flush();
        } catch (IOException e) {
            PlayerLogger.LOGGER.error("写入日志时出错", e);
        }
    }

    public void close() {
        if (logWriter != null) {
            try {
                PlayerLogger.LOGGER.info("关闭日志文件");
                logWriter.flush();
                logWriter.close();
            } catch (IOException e) {
                PlayerLogger.LOGGER.error("关闭日志文件时出错", e);
            }
        }
    }
}