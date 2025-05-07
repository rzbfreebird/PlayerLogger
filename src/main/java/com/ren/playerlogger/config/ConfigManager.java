package com.ren.playerlogger.config;

import com.ren.playerlogger.PlayerLogger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ConfigManager {
    // 配置文件路径
    private static final String CONFIG_FILE = "config/playerlogger.json";

    // 默认配置
    private boolean enabled = true;
    private boolean playerLoginEnabled = true;
    private boolean playerLogoutEnabled = true;
    private boolean logChat = true;
    private boolean logCommands = true;
    private boolean logBlockBreak = true;
    private boolean logBlockPlace = true;  // 添加方块放置配置项
    private boolean logBlockInteraction = true;
    private boolean logItemUse = true;
    private boolean logItemPickup = true;
    private boolean logItemDrop = true;
    private boolean logEntityInteraction = true;
    private boolean logContainerAccess = true;
    private boolean logPlayerDamage = true;  // 玩家伤害配置项

    // 日志设置
    private String logFormat = "[%date%] %player% %action%";  // 支持 txt, csv, json
    private boolean useDailyFiles = true;
    private boolean usePlayerFiles = false;
    private String logDirectory = "logs/playerlogger";

    public ConfigManager() {
        loadConfig();
    }

    // 加载配置文件
    private void loadConfig() {
        try {
            // 确保配置目录存在
            Path configDir = Paths.get("config");
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
            }

            File configFile = new File(CONFIG_FILE);

            // 如果配置文件不存在，创建默认配置
            if (!configFile.exists()) {
                saveConfig();
                return;
            }

            // 加载配置
            JsonObject config = JsonParser.parseReader(new FileReader(configFile)).getAsJsonObject();

            // 读取基本设置
            if (config.has("enabled")) enabled = config.get("enabled").getAsBoolean();
            if (config.has("playerLoginEnabled")) playerLoginEnabled = config.get("playerLoginEnabled").getAsBoolean();
            if (config.has("playerLogoutEnabled")) playerLogoutEnabled = config.get("playerLogoutEnabled").getAsBoolean();
            if (config.has("logChat")) logChat = config.get("logChat").getAsBoolean();
            if (config.has("logCommands")) logCommands = config.get("logCommands").getAsBoolean();
            if (config.has("logBlockBreak")) logBlockBreak = config.get("logBlockBreak").getAsBoolean();
            if (config.has("logBlockPlace")) logBlockPlace = config.get("logBlockPlace").getAsBoolean();  // 读取方块放置配置
            if (config.has("logBlockInteraction")) logBlockInteraction = config.get("logBlockInteraction").getAsBoolean();
            if (config.has("logItemUse")) logItemUse = config.get("logItemUse").getAsBoolean();
            if (config.has("logItemPickup")) logItemPickup = config.get("logItemPickup").getAsBoolean();
            if (config.has("logItemDrop")) logItemDrop = config.get("logItemDrop").getAsBoolean();
            if (config.has("logEntityInteraction")) logEntityInteraction = config.get("logEntityInteraction").getAsBoolean();
            if (config.has("logContainerAccess")) logContainerAccess = config.get("logContainerAccess").getAsBoolean();
            if (config.has("logPlayerDamage")) logPlayerDamage = config.get("logPlayerDamage").getAsBoolean();  // 读取玩家伤害配置

            // 读取日志设置
            if (config.has("logFormat")) logFormat = config.get("logFormat").getAsString();
            if (config.has("useDailyFiles")) useDailyFiles = config.get("useDailyFiles").getAsBoolean();
            if (config.has("usePlayerFiles")) usePlayerFiles = config.get("usePlayerFiles").getAsBoolean();
            if (config.has("logDirectory")) logDirectory = config.get("logDirectory").getAsString();

            PlayerLogger.LOGGER.info("已加载配置文件");
        } catch (Exception e) {
            PlayerLogger.LOGGER.error("加载配置文件时出错，使用默认配置", e);
            saveConfig();  // 出错时保存默认配置
        }
    }

    // 保存配置文件
    public void saveConfig() {
        try {
            JsonObject config = new JsonObject();

            // 写入基本设置
            config.addProperty("enabled", enabled);
            config.addProperty("playerLoginEnabled", playerLoginEnabled);
            config.addProperty("playerLogoutEnabled", playerLogoutEnabled);
            config.addProperty("logChat", logChat);
            config.addProperty("logCommands", logCommands);
            config.addProperty("logBlockBreak", logBlockBreak);
            config.addProperty("logBlockPlace", logBlockPlace);  // 保存方块放置配置
            config.addProperty("logBlockInteraction", logBlockInteraction);
            config.addProperty("logItemUse", logItemUse);
            config.addProperty("logItemPickup", logItemPickup);
            config.addProperty("logItemDrop", logItemDrop);
            config.addProperty("logEntityInteraction", logEntityInteraction);
            config.addProperty("logContainerAccess", logContainerAccess);
            config.addProperty("logPlayerDamage", logPlayerDamage);  // 保存玩家伤害配置

            // 写入日志设置
            config.addProperty("logFormat", logFormat);
            config.addProperty("useDailyFiles", useDailyFiles);
            config.addProperty("usePlayerFiles", usePlayerFiles);
            config.addProperty("logDirectory", logDirectory);

            // 写入文件
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
                gson.toJson(config, writer);
            }

            PlayerLogger.LOGGER.info("已保存配置文件");
        } catch (IOException e) {
            PlayerLogger.LOGGER.error("保存配置文件时出错", e);
        }
    }

    // 添加新的方法以检查是否应该记录玩家伤害
    public boolean shouldLogPlayerDamage() {
        return enabled && logPlayerDamage;
    }

    // 添加新的方法以检查是否应该记录方块放置
    public boolean shouldLogBlockPlace() {
        return enabled && logBlockPlace;
    }

    // 其他getter方法
    public boolean isEnabled() {
        return enabled;
    }

    public boolean isPlayerLoginEnabled() {
        return enabled && playerLoginEnabled;
    }

    public boolean isPlayerLogoutEnabled() {
        return enabled && playerLogoutEnabled;
    }

    public boolean shouldLogChat() {
        return enabled && logChat;
    }

    public boolean shouldLogCommands() {
        return enabled && logCommands;
    }

    public boolean shouldLogBlockBreak() {
        return enabled && logBlockBreak;
    }

    public boolean shouldLogBlockInteraction() {
        return enabled && logBlockInteraction;
    }

    public boolean shouldLogItemUse() {
        return enabled && logItemUse;
    }

    public boolean shouldLogItemPickup() {
        return enabled && logItemPickup;
    }

    public boolean shouldLogItemDrop() {
        return enabled && logItemDrop;
    }

    public boolean shouldLogEntityInteraction() {
        return enabled && logEntityInteraction;
    }

    public boolean shouldLogContainerAccess() {
        return enabled && logContainerAccess;
    }

    public String getLogFormat() {
        return logFormat;
    }

    public boolean isUseDailyFiles() {
        return useDailyFiles;
    }

    public boolean isUsePlayerFiles() {
        return usePlayerFiles;
    }

    public String getLogDirectory() {
        return logDirectory;
    }
}