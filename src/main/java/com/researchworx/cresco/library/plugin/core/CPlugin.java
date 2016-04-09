package com.researchworx.cresco.library.plugin.core;

import com.researchworx.cresco.library.core.Config;
import com.researchworx.cresco.library.core.WatchDog;
import com.researchworx.cresco.library.messaging.MsgEvent;
import com.researchworx.cresco.library.messaging.RPC;
import com.researchworx.cresco.library.utilities.CLogger;
import org.apache.commons.configuration2.SubnodeConfiguration;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

public abstract class CPlugin {
    private String region;
    private String agent;
    private String plugin;

    private String name;
    private String version;

    private boolean isActive;
    private Config config;

    private CExecutor exec;
    private CLogger logger;
    private ConcurrentLinkedQueue<MsgEvent> msgQueue;
    private RPC rpc;
    private ConcurrentMap<String, MsgEvent> rpcMap;
    private WatchDog watchDog;

    public CPlugin() {
        this("unknown", "unknown");
    }

    public CPlugin(String name, String version) {
        this.name = name;
        this.version = version;
        this.region = "init";
        this.agent = "init";
        this.plugin = "init";
        importExecutor();
        this.rpcMap = new ConcurrentHashMap<>();
        this.msgQueue = new ConcurrentLinkedQueue<>();
        this.logger = new CLogger(this.msgQueue, this.region, this.agent, this.plugin);
    }

    public abstract void importExecutor();

    public void shutdown() {
        this.isActive = false;
        this.watchDog.stop();

        try {
            cleanUp();
        } catch (Exception e) {
            logger.error("Plugin Shutdown Exception: {}", e.getMessage());
        }
    }

    public void cleanUp() { }

    public boolean initialize(ConcurrentLinkedQueue<MsgEvent> msgQueue, SubnodeConfiguration config, String region, String agent, String plugin) {
        this.isActive = true;

        this.msgQueue = msgQueue;
        this.config = new Config(config);
        this.region = region;
        this.agent = agent;
        this.plugin = plugin;

        this.logger = new CLogger(this.msgQueue, this.region, this.agent, this.plugin);
        this.rpc = new RPC(this.msgQueue, this.rpcMap, this.region, this.agent, this.plugin, this.logger);
        this.watchDog = new WatchDog(this.region, this.agent, this.plugin, this.logger, this.config);

        try {
            execute();
        } catch (Exception e) {
            this.logger.error("Plugin Initialization Exception: {}", e.getMessage());
            return false;
        }
        return true;
    }

    public abstract void execute();

    public void msgIn(MsgEvent msg) {
        if (msg == null) return;
        new Thread(new MessageProcessor(msg, this.msgQueue, this.exec, this.logger)).start();
    }

    public void sendMessage(MsgEvent msg) {
        this.msgQueue.offer(msg);
    }

    public void sendRPC(MsgEvent msg) {
        this.rpc.send(msg);
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public void setAgent(String agent) {
        this.agent = agent;
    }

    public void setPlugin(String plugin) {
        this.plugin = plugin;
    }

    public void setExec(CExecutor exec) {
        this.exec = exec;
    }

    public void setIsActive(Boolean isActive) {
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
        return msgQueue;
    }

    public CLogger getLogger() {
        return logger;
    }

    public Config getConfig() {
        return config;
    }

    private static class MessageProcessor implements Runnable {
        private MsgEvent msg;
        private ConcurrentLinkedQueue<MsgEvent> msgQueue;
        private CExecutor exec;
        private CLogger logger;

        MessageProcessor(MsgEvent msg, ConcurrentLinkedQueue<MsgEvent> msgQueue, CExecutor exec, CLogger logger) {
            this.msg = msg;
            this.msgQueue = msgQueue;
            this.exec = exec;
            this.logger = logger;
        }

        @Override
        public void run() {
            try {
                MsgEvent retMsg = this.exec.execute(msg);
                if (retMsg != null) {
                    retMsg.setReturn();
                    this.msgQueue.offer(retMsg);
                }
            } catch (Exception e) {
                this.logger.error("Message Execution Exception: {}", e.getMessage());
            }
        }
    }
}