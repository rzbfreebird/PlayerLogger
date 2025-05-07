package com.ren.playerlogger.log.format;

import com.ren.playerlogger.log.events.LogEvent;
import java.text.SimpleDateFormat;

/**
 * CSV格式的日志格式化器
 */
public class CsvLogFormatter implements LogFormatter {
    // 日期格式化工具
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 获取CSV文件的表头
     */
    public String getHeader() {
        return "时间戳,玩家名,事件类型,事件描述";
    }

    @Override
    public String format(LogEvent event) {
        // 构建CSV格式的行
        return DATE_FORMAT.format(event.getTimestamp()) + "," +
                event.getPlayerName() + "," +
                event.getEventType() + "," +
                event.getDescription().replace(",", "\\,");
    }
}