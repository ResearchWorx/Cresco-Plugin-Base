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

/**
 * Cresco plugin base
 * @author V.K. Cody Bumgardner
 * @author Caylin Hickey
 * @version 0.4.1
 */
public abstract class CPlugin {
    /** Region of the plugin instance */
    protected String region;
    /** Agent of the plugin instance */
    protected String agent;
    /** PluginID of the plugin instance */
    protected String pluginID;

    /** Name of the plugin class */
    protected String name;
    /** Version of the plugin class */
    protected String version;

    /** Status of the plugin */
    protected boolean isActive;
    /** Configuration (INI) of the plugin */
    protected Config config;

    /** Executor of the plugin */
    protected CExecutor exec;
    /** Logger of the plugin */
    protected CLogger logger;
    /** Message queue from plugin to agent */
    protected ConcurrentLinkedQueue<MsgEvent> msgOutQueue;
    /** Remote procedural call class of the plugin */
    protected RPC rpc;
    /** Remote procedural call progress map of the plugin */
    protected ConcurrentMap<String, MsgEvent> rpcMap;
    /** WatchDog timer of the plugin */
    protected WatchDog watchDog;

    /**
     * Default constructor
     */
    protected CPlugin() {
        this("unknown", "unknown");
    }

    /**
     * Parameterized constructor
     * @param name          Name of the plugin
     * @param version       Version of the plugin
     */
    protected CPlugin(String name, String version) {
        setName(name);
        setVersion(version);
        setRegion("init");
        setAgent("init");
        setPluginID("init");
        setExecutor();
        setRPCMap(new ConcurrentHashMap<String, MsgEvent>());
        setMsgOutQueue(new ConcurrentLinkedQueue<MsgEvent>());
        setLogger(new CLogger(this.msgOutQueue, this.region, this.agent, this.pluginID));
    }

    /**
     * Initialization method called when the plugin is loaded into the Cresco agent
     * @param msgOutQueue   Linked communication channel from plugin to agent
     * @param config        Subnodeconfiguration object of plugin settings
     * @param region        Region of the plugin
     * @param agent         Agent of the plugin
     * @param pluginID      Internal agent ID for the plugin
     * @return              Whether the initialization was successful
     */
    public boolean initialize(ConcurrentLinkedQueue<MsgEvent> msgOutQueue, SubnodeConfiguration config, String region, String agent, String pluginID) {
        setActive(true);
        setMsgOutQueue(msgOutQueue);
        setConfig(new Config(config));
        setRegion(region);
        setAgent(agent);
        setPluginID(pluginID);
        setLogger(new CLogger(this.msgOutQueue, this.region, this.agent, this.pluginID));
        setRPC(new RPC(this.msgOutQueue, this.rpcMap, this.region, this.agent, this.pluginID, this.logger));
        setWatchDog(new WatchDog(this.region, this.agent, this.pluginID, this.logger, this.config));
        try {
            start();
        } catch (Exception e) {
            this.logger.error("Plugin Initialization Exception: {}", e.getMessage());
            return false;
        }
        startWatchDog();
        return true;
    }

    /**
     * Shutdown method called when the plugin is unloaded from the Cresco agent
     */
    public void shutdown() {
        setActive(false);
        stopWatchDog();
        try {
            cleanUp();
        } catch (Exception e) {
            logger.error("Plugin Shutdown Exception: {}", e.getMessage());
        }
    }

    /**
     * Method to override for any last minute cleanup
     */
    protected void cleanUp() { }

    /**
     * Sets the CExecutor implementation to be used by the plugin
     */
    protected abstract void setExecutor();

    /**
     * Main entry point for plugin implementation class
     */
    protected abstract void start();

    /**
     * Submits a MsgEvent to the CExecutor
     * @param message       Message for the executor
     */
    public MsgEvent execute(MsgEvent message) {
        return this.exec.execute(message);
    }

    /**
     * Incoming messages from the agent to the plugin
     * @param message       Message for the plugin
     */
    public void msgIn(MsgEvent message) {
        if (message == null) return;
        new Thread(new MessageProcessor(message, this.msgOutQueue, this.exec, this.logger)).start();
    }

    /**
     * Puts a message into the RPC map for tracking
     * @param callID        CallID of the message
     * @param message       RPC message to track
     */
    public void putRPCMap(String callID, MsgEvent message) {
        this.rpcMap.put(callID, message);
    }

    /**
     * Sends messages to the Cresco agent
     * @param msg           MsgEvent object to send
     */
    public void sendMsgEvent(MsgEvent msg) {
        this.msgOutQueue.offer(msg);
    }

    /**
     * Issues a remote procedure call to the agent
     * @param msg           MsgEvent object to issue
     */
    public void sendRPC(MsgEvent msg) {
        this.rpc.call(msg);
    }

    /**
     * Starts the WatchDog timer
     */
    public void startWatchDog() {
        this.watchDog.start();
    }

    /**
     * Restarts the WatchDog timer
     */
    public void restartWatchDog() {
        this.watchDog.restart();
    }

    /**
     * Stops the WatchDog timer
     */
    public void stopWatchDog() {
        this.watchDog.stop();
    }

    public String getName() {
        return name;
    }
    protected void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }
    protected void setVersion(String version) {
        this.version = version;
    }

    public String getRegion() {
        return region;
    }
    protected void setRegion(String region) {
        this.region = region;
    }

    public String getAgent() {
        return agent;
    }
    protected void setAgent(String agent) {
        this.agent = agent;
    }

    public String getPluginID() {
        return pluginID;
    }
    protected void setPluginID(String plugin) {
        this.pluginID = plugin;
    }

    public Config getConfig() {
        return config;
    }
    protected void setConfig(Config config) {
        this.config = config;
    }

    public CExecutor getExec() {
        return this.exec;
    }
    protected void setExec(CExecutor exec) {
        this.exec = exec;
    }

    public Boolean isActive() { return isActive; }
    protected void setActive(Boolean state) {
        this.isActive = state;
    }

    public CLogger getLogger() {
        return logger;
    }
    protected void setLogger(CLogger logger) {
        this.logger = logger;
    }

    public ConcurrentLinkedQueue<MsgEvent> getMsgOutQueue() {
        return this.msgOutQueue;
    }
    protected void setMsgOutQueue(ConcurrentLinkedQueue<MsgEvent> msgOutQueue) {
        this.msgOutQueue = msgOutQueue;
    }

    public ConcurrentMap<String, MsgEvent> getRPCMap() {
        return this.rpcMap;
    }
    protected void setRPCMap(ConcurrentMap<String, MsgEvent> rpcMap) {
        this.rpcMap = rpcMap;
    }

    public RPC getRPC() {
        return this.rpc;
    }
    protected void setRPC(RPC rpc) {
        this.rpc = rpc;
    }

    public WatchDog getWatchDog() {
        return this.watchDog;
    }
    protected void setWatchDog(WatchDog watchDog) {
        this.watchDog = watchDog;
    }

    /**
     * Processing agent for incoming messages
     */
    protected static class MessageProcessor implements Runnable {
        /** Incoming MsgEvent object */
        private MsgEvent msg;
        /** Outbound message channel */
        private ConcurrentLinkedQueue<MsgEvent> msgOutQueue;
        /** Cresco executor used to process message */
        private CExecutor exec;
        /** Cresco logger instance */
        private CLogger logger;

        /**
         * Constructor
         * @param msg               MsgEvent to process
         * @param msgOutQueue       Communication channel with Cresco agent
         * @param exec              Execution engine
         * @param logger            Logger instance
         */
        MessageProcessor(MsgEvent msg, ConcurrentLinkedQueue<MsgEvent> msgOutQueue, CExecutor exec, CLogger logger) {
            this.msg = msg;
            this.msgOutQueue = msgOutQueue;
            this.exec = exec;
            this.logger = logger;
        }

        /**
         * Processing method
         */
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