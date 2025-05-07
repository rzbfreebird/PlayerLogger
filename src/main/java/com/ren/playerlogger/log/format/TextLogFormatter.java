package com.ren.playerlogger.log.format;

import com.ren.playerlogger.log.events.LogEvent;
import java.text.SimpleDateFormat;

/**
 * 文本格式的日志格式化器
 */
public class TextLogFormatter implements LogFormatter {
    // 日期格式化工具
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public String format(LogEvent event) {
        // 构建带有时间、玩家名和事件类型的格式化文本
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(DATE_FORMAT.format(event.getTimestamp())).append("] ");
        sb.append("[").append(event.getPlayerName()).append("] ");
        sb.append("[").append(event.getEventType()).append("] ");
        sb.append(event.getDescription());
        return sb.toString();
    }
}