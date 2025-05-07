package com.ren.playerlogger.log.events;

import java.util.Date;

/**
 * 日志事件的抽象基类
 */
public abstract class AbstractLogEvent implements LogEvent {
    // 事件发生时间
    private final Date timestamp;

    // 玩家名称
    private final String playerName;

    // 事件描述
    private final String description;

    /**
     * 创建日志事件
     * @param playerName 玩家名称
     * @param description 事件描述
     */
    public AbstractLogEvent(String playerName, String description) {
        this.timestamp = new Date();
        this.playerName = playerName;
        this.description = description;
    }

    /**
     * 获取事件类型
     */
    public abstract String getEventType();

    /**
     * 获取事件时间戳
     */
    public Date getTimestamp() {
        return timestamp;
    }

    /**
     * 获取玩家名称
     */
    public String getPlayerName() {
        return playerName;
    }

    /**
     * 获取事件描述
     */
    public String getDescription() {
        return description;
    }
}