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
                incoming = processConfigWrapper(incoming);
            } else if (incoming.getMsgType().equals(MsgEvent.Type.DISCOVER)) {
                incoming = processDiscoverWrapper(incoming);
            } else if (incoming.getMsgType().equals(MsgEvent.Type.ERROR)) {
                incoming = processConfigWrapper(incoming);
            } else if (incoming.getMsgType().equals(MsgEvent.Type.EXEC)) {
                incoming = processExecWrapper(incoming);
            } else if (incoming.getMsgType().equals(MsgEvent.Type.INFO)) {
                incoming = processInfoWrapper(incoming);
            } else if (incoming.getMsgType().equals(MsgEvent.Type.WATCHDOG)) {
                incoming = processWatchDogWrapper(incoming);
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
    public MsgEvent processConfig(MsgEvent incoming) {
        return incoming;
    }
    public MsgEvent processDiscoverWrapper(MsgEvent incoming) {
        setUnsupported(incoming);
        processDiscover(incoming);
        return incoming;
    }
    public MsgEvent processDiscover(MsgEvent incoming) {
        return incoming;
    }
    public MsgEvent processErrorWrapper(MsgEvent incoming) {
        setUnsupported(incoming);
        processError(incoming);
        return incoming;
    }
    public MsgEvent processError(MsgEvent incoming) {
        return incoming;
    }
    public MsgEvent processExecWrapper(MsgEvent incoming) {
        setUnsupported(incoming);
        processExec(incoming);
        return incoming;
    }
    public MsgEvent processExec(MsgEvent incoming) {
        return incoming;
    }
    public MsgEvent processInfoWrapper(MsgEvent incoming) {
        setUnsupported(incoming);
        processInfo(incoming);
        return incoming;
    }
    public MsgEvent processInfo(MsgEvent incoming) {
        return incoming;
    }
    public MsgEvent processWatchDogWrapper(MsgEvent incoming) {
        setUnsupported(incoming);
        processWatchDog(incoming);
        return incoming;
    }
    public MsgEvent processWatchDog(MsgEvent incoming) {
        return incoming;
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
