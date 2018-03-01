package com.researchworx.cresco.library.plugin.utilities;

import com.researchworx.cresco.library.messaging.MsgEvent;

import java.util.Date;
import java.util.concurrent.BlockingQueue;

/**
 * Cresco logger
 * @author V.K. Cody Bumgardner
 * @author Caylin Hickey
 * @since 0.1.0
 */
public class CLogger {
    public enum Level {
        None(-1), Error(0), Warn(1), Info(2), Debug(4), Trace(8);
        private final int level;
        Level(int level) { this.level = level; }
        public int getValue() { return level; }
        public boolean toShow(Level check) {
            return check.getValue() <= this.getValue();
        }
    }
    private Level level;
    private BlockingQueue<MsgEvent> msgOutQueue;
    private Class issuingClass;

    public CLogger(BlockingQueue<MsgEvent> msgOutQueue) {
        this(msgOutQueue, Level.Info);
    }

    public CLogger(BlockingQueue<MsgEvent> msgOutQueue, Level level) {
        this.level = level;
        this.msgOutQueue = msgOutQueue;
    }

    public CLogger(Class issuingClass, BlockingQueue<MsgEvent> msgOutQueue) {
        this(issuingClass, msgOutQueue, Level.Info);
    }

    public CLogger(Class issuingClass, BlockingQueue<MsgEvent> msgOutQueue, Level level) {
        this.level = level;
        this.issuingClass = issuingClass;
        this.msgOutQueue = msgOutQueue;
    }

    public void error(String logMessage) {
        if (!level.toShow(Level.Error)) return;
        log(logMessage, Level.Error);
    }

    public void error(String logMessage, Object ... params) {
        if (!level.toShow(Level.Error)) return;
        error(replaceBrackets(logMessage, params));
    }

    public void warn(String logMessage) {
        if (!level.toShow(Level.Warn)) return;
        log(logMessage, Level.Warn);
    }

    public void warn(String logMessage, Object ... params) {
        if (!level.toShow(Level.Warn)) return;
        warn(replaceBrackets(logMessage, params));
    }

    public void info(String logMessage) {
        if (!level.toShow(Level.Info)) return;
        log(logMessage, Level.Info);
    }

    public void info(String logMessage, Object ... params) {
        if (!level.toShow(Level.Info)) return;
        info(replaceBrackets(logMessage, params));
    }

    public void debug(String logMessage) {
        if (!level.toShow(Level.Debug)) return;
        log(logMessage, Level.Debug);
    }

    public void debug(String logMessage, Object ... params) {
        if (!level.toShow(Level.Debug)) return;
        debug(replaceBrackets(logMessage, params));
    }

    public void trace(String logMessage) {
        if (!level.toShow(Level.Trace)) return;
        log(logMessage, Level.Trace);
    }

    public void trace(String logMessage, Object ... params) {
        if (!level.toShow(Level.Trace)) return;
        trace(replaceBrackets(logMessage, params));
    }

    public void log(String logMessage, Level level) {
        MsgEvent toSend = new MsgEvent(MsgEvent.Type.LOG);
        // ToDo: Rework to use Static CState class after implementation
        //toSend.setDestination(new CAddr(MsgEvent.getMyAddress().getRegion()));
        toSend.setParam("log_message", logMessage);
        if (issuingClass != null) {
            toSend.setParam("log_class", issuingClass.getSimpleName());
            toSend.setParam("log_full_class", issuingClass.getCanonicalName());
        }
        toSend.setParam("log_ts", String.valueOf(new Date().getTime()));
        toSend.setParam("log_level", level.name());
        log(toSend);
    }

    public void log(MsgEvent logMessage) {
        msgOutQueue.offer(logMessage);
    }

    public Level getLogLevel() {
        return level;
    }

    public void setLogLevel(Level level) {
        this.level = level;
    }

    private String replaceBrackets(String logMessage, Object ... params) {
        int replaced = 0;
        while (logMessage.contains("{}") && replaced < params.length) {
            logMessage = logMessage.replaceFirst("\\{\\}", String.valueOf(params[replaced]));
            replaced++;
        }
        return logMessage;
    }
}
