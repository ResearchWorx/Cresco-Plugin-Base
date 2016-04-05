package com.researchworx.cresco.plugins.messaging;

import com.researchworx.cresco.plugins.utilities.Clogger;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

public class RPC {
    private Clogger logger;
    private ConcurrentLinkedQueue<MsgEvent> msgQueue;
    private ConcurrentMap<String, MsgEvent> rpcMap;
    private String region;
    private String agent;
    private String plugin;

    public RPC(ConcurrentLinkedQueue<MsgEvent> msgQueue, ConcurrentMap<String, MsgEvent> rpcMap, String region, String agent, String plugin, Clogger logger) {
        this.logger = logger;
        this.msgQueue = msgQueue;
        this.rpcMap = rpcMap;
        this.region = region;
        this.agent = agent;
        this.plugin = plugin;
    }

    public MsgEvent call(MsgEvent me) {
        try {
            String callId = java.util.UUID.randomUUID().toString();
            me.setParam("callId-" + this.region + "-" + this.agent + "-" + this.plugin, callId);
            this.msgQueue.offer(me);

            int count = 0;
            int timeout = 300;
            while (count < timeout) {
                if (this.rpcMap.containsKey(callId)) {
                    MsgEvent ce;
                    synchronized (this.rpcMap) {
                        ce = this.rpcMap.get(callId);
                        this.rpcMap.remove(callId);
                    }
                    return ce;
                }
                Thread.sleep(100);
                count++;
            }

            return null;
        } catch (Exception ex) {
            this.logger.error("Controller : RPCCall : RPC failed {}", ex.toString());
            return null;
        }

    }
}
