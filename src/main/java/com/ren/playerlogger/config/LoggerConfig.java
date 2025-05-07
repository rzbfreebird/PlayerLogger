package com.ren.playerlogger.config;

public class LoggerConfig {
    public boolean enabled = true;
    public String logFormat = "[%player%] %action% at %position%";
    public boolean printToConsole = false;
    public LogEvents logEvents = new LogEvents();

    public static class LogEvents {
        public boolean playerLogin = true;
        public boolean playerLogout = true;
        public boolean blockBreak = true;
        public boolean blockPlace = true;
        public boolean blockInteraction = true;
        public boolean itemUse = true;
        public boolean itemDrop = true;
        public boolean itemPickup = true;
        public boolean chat = true;
        public boolean command = true;
        public boolean entityInteraction = true;
        public boolean playerDamage = true;
        public boolean playerKill = true;
        public boolean containerAccess = true;
    }
}