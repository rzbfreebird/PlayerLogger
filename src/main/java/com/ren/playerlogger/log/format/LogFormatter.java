package com.ren.playerlogger.log.format;

import com.ren.playerlogger.log.events.LogEvent;

/**
 * 日志格式化器接口
 */
public interface LogFormatter {
    /**
     * 将日志事件格式化为字符串
     */
    String format(LogEvent event);
}