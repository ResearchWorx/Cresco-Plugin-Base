package com.researchworx.cresco.library.plugin.utilities;

import com.researchworx.cresco.library.messaging.CAddr;
import com.researchworx.cresco.library.messaging.MsgEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CLoggerTest {
    //private final Logger logger = LoggerFactory.getLogger(CLoggerTest.class);
    private final CAddr source = new CAddr("test_src_region", "test_src_agent", "test_src_plugin");
    private final CAddr destination = new CAddr("test_dst_region", "test_dst_agent", "test_dst_plugin");
    private FakeAgent fakeAgent;
    private BlockingQueue<MsgEvent> msgOutQueue;

    @Before
    public void setUp() {
        msgOutQueue = new LinkedBlockingQueue<>();
        fakeAgent = new FakeAgent(msgOutQueue);
        new Thread(fakeAgent).start();
    }

    @Test
    public void Test1_FakeAgent() {
        MsgEvent msgEvent = new MsgEvent(MsgEvent.Type.LOG, source, destination);
        msgEvent.setParam("log_message", "Test Log Message");
        msgEvent.setParam("log_class", CLoggerTest.class.getSimpleName());
        msgEvent.setParam("log_full_class", CLoggerTest.class.getCanonicalName());
        msgEvent.setParam("log_ts", String.valueOf(new Date().getTime()));
        msgEvent.setParam("log_level", CLogger.Level.Info.name());
        msgOutQueue.offer(msgEvent);
    }

    @Test
    public void Test2_Logger() {
        CLogger cLogger = new CLogger(msgOutQueue);
        cLogger.info("Test CLogger Message");
    }

    @After
    public void tearDown() {
        fakeAgent.stop();
    }

    class FakeAgent implements Runnable {
        private final Logger logger = LoggerFactory.getLogger(FakeAgent.class);
        BlockingQueue<MsgEvent> msgInQueue;
        boolean running;

        FakeAgent(BlockingQueue<MsgEvent> msgInQueue) {
            logger.info("FakeAgent starting");
            this.msgInQueue = msgInQueue;
            this.running = true;
        }

        void stop() {
            logger.info("FakeAgent stopping");
            this.running = false;
        }

        @Override
        public void run() {
            while (running) {
                try {
                    MsgEvent msgEvent = msgInQueue.take();
                    if (msgEvent.getType().equals(MsgEvent.Type.LOG)) {
                        String className = msgEvent.getParam("log_full_class");
                        String logMessage = "[" + msgEvent.getSourcePlugin() + ": FAKE_AGENT_PLUGIN_NAME]";
                        if (className != null)
                            logMessage = logMessage + "[" + formatClassName(className) + "]";
                        logMessage = logMessage + " " + msgEvent.getParam("log_message");
                        switch (msgEvent.getParam("log_level").toLowerCase()) {
                            case "error":
                                logger.error(logMessage);
                                break;
                            case "warn":
                                logger.warn(logMessage);
                                break;
                            case "info":
                                logger.info(logMessage);
                                break;
                            case "debug":
                                logger.debug(logMessage);
                                break;
                            case "trace":
                                logger.trace(logMessage);
                                break;
                            default:
                                logger.error("Unknown log_level [{}]", msgEvent.getParam("log_level"));
                                break;
                        }
                    }
                } catch (InterruptedException ex) {
                    // Ignore
                }
            }
        }

        private String formatClassName(String className) {
            StringBuilder newName = new StringBuilder("");
            int lastIndex = 0;
            int nextIndex = className.indexOf(".", lastIndex + 1);
            while (nextIndex != -1) {
                newName.append(className.substring(lastIndex, lastIndex + 1));
                newName.append(".");
                lastIndex = nextIndex + 1;
                nextIndex = className.indexOf(".", lastIndex + 1);
            }
            return newName + className.substring(lastIndex);
        }
    }
}
