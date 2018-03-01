package com.researchworx.cresco.library.plugin.messaging;

import com.researchworx.cresco.library.messaging.MsgEvent;
import com.researchworx.cresco.library.plugin.utilities.CLogger;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Cresco remote procedure call helper
 * @author V.K. Cody Bumgardner
 * @author Caylin Hickey
 * @since 0.1.0
 */
public class RPC {
    /** Time between checks for RPC return message (in milliseconds) */
    private static final int CHECK_INTERVAL = 100;
    /** Maximum iterations to check for RPC return message */
    private static final int MAX_INTERVALS = 300;
    /** Cresco logger */
    private CLogger logger;
    /** Communication channel */
    private BlockingQueue<MsgEvent> msgOutQueue;
    /** Central RPC return message repository */
    private ConcurrentMap<String, MsgEvent> rpcMap = new ConcurrentHashMap<>();
    /** Region of this instance */
    private String region;
    /** Agent of this instance */
    private String agent;
    /** Plugin ID of this instance */
    private String pluginID;

    /**
     * Constructor
     * @param msgOutQueue   Outbound communication channel
     * @param region        Region of this instance
     * @param agent         Agent of this instance
     * @param pluginID      Plugin ID of this instance
     * @param logger        Logger to use
     */
    public RPC(BlockingQueue<MsgEvent> msgOutQueue, String region, String agent, String pluginID, CLogger logger) {
        this.logger = logger;
        this.msgOutQueue = msgOutQueue;
        this.region = region;
        this.agent = agent;
        this.pluginID = pluginID;
    }

    /**
     * Updates the identity broadcast by this WatchDog instance
     * @param region        New Region to report from
     * @param agent         New Agent to report from
     * @param pluginID      New Plugin ID to report from
     */
    public void update(String region, String agent, String pluginID) {
        setRegion(region);
        setAgent(agent);
        setPluginID(pluginID);
    }

    /**
     * Issues a remote procedure call
     * @param msg           Message to send
     * @return              The return message, null if no return is received
     */
    public MsgEvent call(MsgEvent msg) {
        try {
            String callId = java.util.UUID.randomUUID().toString();
            msg.setParam("callId-" + region + "-" + agent + "-" + pluginID, callId);
            msgOutQueue.add(msg);

            int count = 0;
            while (count++ < MAX_INTERVALS) {
                if (rpcMap.containsKey(callId)) {
                    MsgEvent callBack;
                    callBack = rpcMap.get(callId);
                    rpcMap.remove(callId);
                    return callBack;
                }
                Thread.sleep(CHECK_INTERVAL);
            }
        } catch (Exception ex) {
            logger.error("call {}", ex.getMessage());
        }
        return null;
    }

    /**
     * Places the return message for retrieval
     * @param callId            ID of the remote-procedural call
     * @param returnMessage     The return message
     */
    public void putReturnMessage(String callId, MsgEvent returnMessage) {
        rpcMap.put(callId, returnMessage);
    }

    /**
     * Region identification getter
     * @return          Current region identification
     */
    public String getRegion() {
        return region;
    }

    /**
     * Region identification setter
     * @param region    New region identification
     */
    public void setRegion(String region) {
        this.region = region;
    }

    /**
     * Agent identification getter
     * @return          Current agent identification
     */
    public String getAgent() {
        return agent;
    }

    /**
     * Agent identification setter
     * @param agent     New agent identification
     */
    public void setAgent(String agent) {
        this.agent = agent;
    }

    /**
     * Plugin identification getter
     * @return          Current plugin identification
     */
    public String getPluginID() {
        return pluginID;
    }

    /**
     * Plugin identification setter
     * @param pluginID  New plugin identification
     */
    public void setPluginID(String pluginID) {
        this.pluginID = pluginID;
    }

    /**
     * Logger instance getter
     * @return          Current logger instance
     */
    public CLogger getLogger() {
        return logger;
    }

    /**
     * Logger instance setter
     * @param logger    New logger instance
     */
    public void setLogger(CLogger logger) {
        this.logger = logger;
    }
}
