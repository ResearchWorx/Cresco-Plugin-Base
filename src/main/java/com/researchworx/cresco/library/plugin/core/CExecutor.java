package com.researchworx.cresco.library.plugin.core;

import com.researchworx.cresco.library.messaging.MsgEvent;

public abstract class CExecutor {
    protected CPlugin plugin;

    public CExecutor(CPlugin plugin) {
        this.plugin = plugin;
    }

    public MsgEvent execute(MsgEvent incoming) {
        String callID = getCallID(incoming);
        if (callID != null) {
            this.plugin.setRPCMap(callID, incoming);
            return null;
        } else if (incoming.getParam("dst_region").equals(this.plugin.getRegion()) &&
                incoming.getParam("dst_agent").equals(this.plugin.getAgent()) &&
                incoming.getParam("dst_plugin").equals(this.plugin.getPlugin())) {
            if (incoming.getMsgType().equals(MsgEvent.Type.CONFIG)) {
                processConfigWrapper(incoming);
            } else if (incoming.getMsgType().equals(MsgEvent.Type.DISCOVER)) {
                processDiscoverWrapper(incoming);
            } else if (incoming.getMsgType().equals(MsgEvent.Type.ERROR)) {
                processConfigWrapper(incoming);
            } else if (incoming.getMsgType().equals(MsgEvent.Type.EXEC)) {
                processExecWrapper(incoming);
            } else if (incoming.getMsgType().equals(MsgEvent.Type.INFO)) {
                processInfoWrapper(incoming);
            } else if (incoming.getMsgType().equals(MsgEvent.Type.WATCHDOG)) {
                processWatchDogWrapper(incoming);
            } else {
                incoming.setMsgBody("Unknown or Unset MsgEvent.Type: " + incoming.getParams());
            }
        } else {
            incoming.setMsgPlugin(incoming.getParam("dst_plugin"));
            incoming.setMsgAgent(incoming.getParam("dst_agent"));
            return null;
        }
        return incoming;
    }

    public MsgEvent processConfigWrapper(MsgEvent incoming) {
        setUnsupported(incoming);
        processConfig(incoming);
        return incoming;
    }
    public void processConfig(MsgEvent incoming) {

    }
    public MsgEvent processDiscoverWrapper(MsgEvent incoming) {
        setUnsupported(incoming);
        processDiscover(incoming);
        return incoming;
    }
    public void processDiscover(MsgEvent incoming) {

    }
    public MsgEvent processErrorWrapper(MsgEvent incoming) {
        setUnsupported(incoming);
        processError(incoming);
        return incoming;
    }
    public void processError(MsgEvent incoming) {

    }
    public MsgEvent processExecWrapper(MsgEvent incoming) {
        setUnsupported(incoming);
        processExec(incoming);
        return incoming;
    }
    public void processExec(MsgEvent incoming) {

    }
    public MsgEvent processInfoWrapper(MsgEvent incoming) {
        setUnsupported(incoming);
        processInfo(incoming);
        return incoming;
    }
    public void processInfo(MsgEvent incoming) {

    }
    public MsgEvent processWatchDogWrapper(MsgEvent incoming) {
        setUnsupported(incoming);
        processWatchDog(incoming);
        return incoming;
    }
    public void processWatchDog(MsgEvent incoming) {

    }

    private MsgEvent setUnsupported(MsgEvent incoming) {
        incoming.setMsgBody("Message type [" + incoming.getMsgType().name() + "] unsupported by plugin [" + this.plugin.getName() + ":" + this.plugin.getVersion() + "]");
        return incoming;
    }

    protected String getCallID(MsgEvent msg) {
        return msg.getParam("callID-" + this.plugin.getRegion() + "-" +
                this.plugin.getAgent() + "-" + this.plugin.getPlugin());
    }
}
