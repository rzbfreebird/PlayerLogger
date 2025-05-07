package com.ren.playerlogger.log.events;

import java.util.Date;

/**
 * 日志事件接口
 */
public interface LogEvent {
    /**
     * 获取事件时间戳
     */
    Date getTimestamp();

    /**
     * 获取玩家名称
     */
    String getPlayerName();

    /**
     * 获取事件类型
     */
    String getEventType();

    /**
     * 获取事件描述
     */
    String getDescription();
}