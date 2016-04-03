package com.researchworx.cresco.plugins.base.messaging;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlRootElement
public class MsgEvent {

    //private String msgType;
    private MsgEventType msgType;
    private String msgRegion;
    private String msgAgent;
    private String msgPlugin;
    private Map<String, String> params;

    public MsgEvent() {

    }

    public MsgEvent(MsgEventType msgType, String msgRegion, String msgAgent, String msgPlugin, Map<String, String> params) {
        this.msgType = msgType;
        //this.mType = MsgEventType.CONFIG;
        this.msgRegion = msgRegion;
        this.msgAgent = msgAgent;
        this.msgPlugin = msgPlugin;
        this.params = params;
        this.params = new HashMap<String, String>(params);

    }

    public MsgEvent(MsgEventType msgType, String msgRegion, String msgAgent, String msgPlugin, String msgBody) {
        this.msgType = msgType;
        //this.mType = MsgEventType.CONFIG;
        this.msgRegion = msgRegion;
        this.msgAgent = msgAgent;
        this.msgPlugin = msgPlugin;
        this.params = new HashMap<String, String>();
        params.put("msg", msgBody);
    }

    public void setSrc(String region, String agent, String plugin) {
        setParam("src_region", region);
        setParam("src_agent", agent);
        setParam("src_plugin", plugin);
    }

    public void setDst(String region, String agent, String plugin) {
        setParam("dst_region", region);
        setParam("dst_agent", agent);
        setParam("dst_plugin", plugin);
    }

    public void setReturn() {

        String src_region = new String(getParam("src_region"));
        String src_agent = new String(getParam("src_agent"));
        String src_plugin = null;
        if (getParam("src_plugin") != null) //plugin
        {
            src_plugin = new String(getParam("src_plugin"));
        }


        params.remove("src_region");
        params.remove("src_agent");
        params.remove("src_plugin");

        params.put("src_region", params.get("dst_region"));
        params.put("src_agent", params.get("dst_agent"));
        if (params.get("dst_plugin") != null) {
            params.put("src_plugin", params.get("dst_plugin"));
        }

        params.put("dst_region", src_region);
        setMsgRegion(src_region);
        params.put("dst_agent", src_agent);
        setMsgAgent(src_agent);

        if (src_plugin != null) {
            params.put("dst_plugin", src_plugin);
            setMsgPlugin(src_plugin);
        } else {
            params.remove("dst_plugin");
            setMsgPlugin(null);
        }


    }

    public String getMsgBody() {
        return params.get("msg");
    }

    public void setMsgBody(String msgBody) {
        params.put("msg", msgBody);
    }

    @XmlJavaTypeAdapter(MsgEventTypesAdapter.class)
    public MsgEventType getMsgType() {
        return msgType;
    }

    public void setMsgType(MsgEventType msgType) {
        this.msgType = msgType;
    }

    public String getMsgRegion() {
        return msgRegion;
    }

    public void setMsgRegion(String msgRegion) {
        this.msgRegion = msgRegion;
    }

    public String getMsgAgent() {
        return msgAgent;
    }

    public void setMsgAgent(String msgAgent) {
        this.msgAgent = msgAgent;
    }

    public String getMsgPlugin() {
        return msgPlugin;
    }

    public void setMsgPlugin(String msgPlugin) {
        this.msgPlugin = msgPlugin;
    }


    @XmlJavaTypeAdapter(MsgEventParamsAdapter.class)
    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    public String getParam(String key) {
        if (params.containsKey(key)) {
            return params.get(key);
        }
        return null;
    }

    public void setParam(String key, String value) {
        params.put(key, value);
    }

    public void removeParam(String key) {
        params.remove(key);
    }

    public String getParamsString() {

        Map<String, String> tmpMap = new HashMap<String, String>(params);
        //tmpMap.keySet().removeAll(params.keySet());
        //params.putAll(tmpMap);
        //target.putAll(tmp);

        StringBuilder sb = new StringBuilder();

        Iterator it = tmpMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry) it.next();
            sb.append(pairs.getKey() + " = " + pairs.getValue() + "\n");
            //System.out.println(pairs.getKey() + " = " + pairs.getValue());
            it.remove(); // avoids a ConcurrentModificationException
        }

        return sb.toString();
    }

}