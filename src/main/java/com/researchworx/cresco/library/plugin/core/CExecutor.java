package com.researchworx.cresco.library.plugin.core;

import com.researchworx.cresco.library.messaging.MsgEvent;

public abstract class CExecutor {
    /** Plugin instance */
    protected CPlugin plugin;

    /**
     * Constructor
     * @param plugin        Plugin instance for this Cresco executor
     */
    public CExecutor(CPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Process incoming message
     * @param incoming      Incoming message
     * @return              Processed message
     */
    public MsgEvent execute(MsgEvent incoming) {
        String callID = incoming.getParam("callID-" + this.plugin.getRegion() + "-" +
                this.plugin.getAgent() + "-" + this.plugin.getPluginID());
        if (callID != null) {
            this.plugin.putRPCMap(callID, incoming);
            return null;
        }
        if (incoming.getParam("dst_region").equals(this.plugin.getRegion()) &&
                incoming.getParam("dst_agent").equals(this.plugin.getAgent()) &&
                incoming.getParam("dst_plugin").equals(this.plugin.getPluginID())) {
            if (incoming.getMsgType().equals(MsgEvent.Type.CONFIG)) {
                incoming = processConfig(incoming);
            } else if (incoming.getMsgType().equals(MsgEvent.Type.DISCOVER)) {
                incoming = processDiscover(incoming);
            } else if (incoming.getMsgType().equals(MsgEvent.Type.ERROR)) {
                incoming = processConfig(incoming);
            } else if (incoming.getMsgType().equals(MsgEvent.Type.EXEC)) {
                incoming = processExec(incoming);
            } else if (incoming.getMsgType().equals(MsgEvent.Type.INFO)) {
                incoming = processInfo(incoming);
            } else if (incoming.getMsgType().equals(MsgEvent.Type.WATCHDOG)) {
                incoming = processWatchDog(incoming);
            } else {
                incoming.setMsgBody("Message type [" + incoming.getMsgType().name() + "] unsupported by plugin [" + this.plugin.getName() + ":" + this.plugin.getVersion() + "]");
            }
        } else {
            incoming.setMsgPlugin(incoming.getParam("dst_plugin"));
            incoming.setMsgAgent(incoming.getParam("dst_agent"));
            incoming.setMsgRegion(incoming.getParam("dst_region"));
            return null;
        }
        return incoming;
    }

    /**
     * Override to process Config messages
     * @param incoming      Message to process
     * @return              Processed message
     */
    public MsgEvent processConfig(MsgEvent incoming) {
        return incoming;
    }
    /**
     * Override to process Discovier messages
     * @param incoming      Message to process
     * @return              Processed message
     */
    public MsgEvent processDiscover(MsgEvent incoming) {
        return incoming;
    }
    /**
     * Override to process Error messages
     * @param incoming      Message to process
     * @return              Processed message
     */
    public MsgEvent processError(MsgEvent incoming) {
        return incoming;
    }
    /**
     * Override to process Exec messages
     * @param incoming      Message to process
     * @return              Processed message
     */
    public MsgEvent processExec(MsgEvent incoming) {
        return incoming;
    }
    /**
     * Override to process Info messages
     * @param incoming      Message to process
     * @return              Processed message
     */
    public MsgEvent processInfo(MsgEvent incoming) {
        return incoming;
    }
    /**
     * Override to process WatchDog messages
     * @param incoming      Message to process
     * @return              Processed message
     */
    public MsgEvent processWatchDog(MsgEvent incoming) {
        return incoming;
    }
}
