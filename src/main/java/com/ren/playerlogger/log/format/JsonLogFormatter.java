package com.ren.playerlogger.log.format;

import com.ren.playerlogger.log.events.LogEvent;
import java.text.SimpleDateFormat;

/**
 * JSON格式的日志格式化器
 */
public class JsonLogFormatter implements LogFormatter {
    // 日期格式化工具
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public String format(LogEvent event) {
        // 手动构建简单的JSON对象
        return "{" +
                "\"timestamp\":\"" + DATE_FORMAT.format(event.getTimestamp()) + "\"," +
                "\"player\":\"" + event.getPlayerName() + "\"," +
                "\"eventType\":\"" + event.getEventType() + "\"," +
                "\"description\":\"" + event.getDescription().replace("\"", "\\\"") + "\"" +
                "}";
    }
}