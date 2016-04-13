package com.researchworx.cresco.library.plugin.core;

import com.researchworx.cresco.library.core.Config;
import com.researchworx.cresco.library.core.WatchDog;
import com.researchworx.cresco.library.messaging.MsgEvent;
import com.researchworx.cresco.library.messaging.RPC;
import com.researchworx.cresco.library.utilities.CLogger;
import org.apache.commons.configuration.SubnodeConfiguration;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

public abstract class CPlugin {
    protected String region;
    protected String agent;
    protected String plugin;

    protected String name;
    protected String version;

    protected boolean isActive;
    protected Config config;

    protected CExecutor exec;
    protected CLogger logger;
    protected ConcurrentLinkedQueue<MsgEvent> msgOutQueue;
    protected RPC rpc;
    protected ConcurrentMap<String, MsgEvent> rpcMap;
    protected WatchDog watchDog;

    protected CPlugin() {
        this("unknown", "unknown");
    }

    protected CPlugin(String name, String version) {
        this.name = name;
        this.version = version;
        this.region = "init";
        this.agent = "init";
        this.plugin = "init";
        importExecutor();
        this.rpcMap = new ConcurrentHashMap<>();
        this.msgOutQueue = new ConcurrentLinkedQueue<>();
        this.logger = new CLogger(this.msgOutQueue, this.region, this.agent, this.plugin);
    }

    protected abstract void importExecutor();

    public void shutdown() {
        this.isActive = false;
        this.watchDog.stop();

        try {
            cleanUp();
        } catch (Exception e) {
            logger.error("Plugin Shutdown Exception: {}", e.getMessage());
        }
    }

    protected void cleanUp() { }

    public boolean initialize(ConcurrentLinkedQueue<MsgEvent> msgOutQueue, SubnodeConfiguration config, String region, String agent, String plugin) {
        this.isActive = true;

        this.msgOutQueue = msgOutQueue;
        this.config = new Config(config);
        this.region = region;
        this.agent = agent;
        this.plugin = plugin;

        this.logger = new CLogger(this.msgOutQueue, this.region, this.agent, this.plugin);
        this.rpc = new RPC(this.msgOutQueue, this.rpcMap, this.region, this.agent, this.plugin, this.logger);
        this.watchDog = new WatchDog(this.region, this.agent, this.plugin, this.logger, this.config);

        try {
            execute();
        } catch (Exception e) {
            this.logger.error("Plugin Initialization Exception: {}", e.getMessage());
            return false;
        }
        return true;
    }

    protected abstract void execute();

    public void msgIn(MsgEvent msg) {
        if (msg == null) return;
        new Thread(new MessageProcessor(msg, this.msgOutQueue, this.exec, this.logger)).start();
    }

    public void sendMsgEvent(MsgEvent msg) {
        this.msgOutQueue.offer(msg);
    }

    public void sendRPC(MsgEvent msg) {
        this.rpc.call(msg);
    }

    protected void setRegion(String region) {
        this.region = region;
    }

    protected void setAgent(String agent) {
        this.agent = agent;
    }

    protected void setPlugin(String plugin) {
        this.plugin = plugin;
    }

    protected void setExec(CExecutor exec) {
        this.exec = exec;
    }

    protected void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public String getRegion() {
        return region;
    }

    public String getAgent() {
        return agent;
    }

    public String getPlugin() {
        return plugin;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public RPC getRPC() {
        return rpc;
    }

    public ConcurrentMap<String, MsgEvent> getRPCMap() {
        return rpcMap;
    }

    public CExecutor getExec() {
        return exec;
    }

    public ConcurrentLinkedQueue<MsgEvent> getMsgQueue() {
        return msgOutQueue;
    }

    public CLogger getLogger() {
        return logger;
    }

    public Config getConfig() {
        return config;
    }

    private static class MessageProcessor implements Runnable {
        private MsgEvent msg;
        private ConcurrentLinkedQueue<MsgEvent> msgOutQueue;
        private CExecutor exec;
        private CLogger logger;

        MessageProcessor(MsgEvent msg, ConcurrentLinkedQueue<MsgEvent> msgOutQueue, CExecutor exec, CLogger logger) {
            this.msg = msg;
            this.msgOutQueue = msgOutQueue;
            this.exec = exec;
            this.logger = logger;
        }

        @Override
        public void run() {
            try {
                MsgEvent retMsg = this.exec.execute(msg);
                if (retMsg != null) {
                    retMsg.setReturn();
                    this.msgOutQueue.offer(retMsg);
                }
            } catch (Exception e) {
                this.logger.error("Message Execution Exception: {}", e.getMessage());
            }
        }
    }
}