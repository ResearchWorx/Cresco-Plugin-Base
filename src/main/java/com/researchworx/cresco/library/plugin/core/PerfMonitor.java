package com.researchworx.cresco.library.plugin.core;

import com.researchworx.cresco.library.core.Config;
import com.researchworx.cresco.library.messaging.MsgEvent;
import com.researchworx.cresco.library.utilities.CLogger;

import java.util.Timer;
import java.util.TimerTask;

public class PerfMonitor {
    private Timer timer;
    private boolean running = false;
    private long interval;
    private long startTS;

    private String region;
    private String agent;
    private String plugin;
    private CLogger clog;
    private Config config;

    public PerfMonitor(String region, String agent, String plugin, CLogger clog, Config config) {
        this.region = region;
        this.agent = agent;
        this.plugin = plugin;
        this.clog = clog;
        this.config = config;

        this.interval = Long.parseLong(config.getStringParam("", "watchdogtimer"));
        this.startTS = System.currentTimeMillis();

        MsgEvent initial = new MsgEvent(MsgEvent.Type.INFO, this.region, this.agent, this.plugin, "WatchDog timer set to " + this.interval + " milliseconds");
        initial.setParam("src_region", this.region);
        initial.setParam("src_agent", this.agent);
        initial.setParam("src_plugin", this.plugin);
        initial.setParam("dst_region", this.region);
        this.clog.log(initial);

        start();
    }

    private class PerfMonitorTask extends TimerTask {
        private String region;
        private String agent;
        private String plugin;
        private Long interval;
        private CLogger clog;
        private Config config;

        PerfMonitorTask(String region, String agent, String plugin, Long interval, CLogger clog, Config config) {
            this.region = region;
            this.agent = agent;
            this.plugin = plugin;
            this.interval = interval;
            this.clog = clog;
            this.config = config;
        }

        public void run() {
            long runTime = System.currentTimeMillis() - startTS;
            MsgEvent le = new MsgEvent(MsgEvent.Type.WATCHDOG, this.region, null, null, "WatchDog timer set to " + this.interval + " milliseconds");
            le.setParam("src_region", this.region);
            le.setParam("src_agent", this.agent);
            le.setParam("src_plugin", this.plugin);
            le.setParam("dst_region", this.region);
            le.setParam("isGlobal", "true");
            le.setParam("resource_id", this.config.getStringParam("", ""));
            le.setParam("inode_id", this.config.getStringParam("", ""));
            le.setParam("perfmetric", this.config.getStringParam("", ""));
            le.setParam("runtime", String.valueOf(runTime));
            le.setParam("timestamp", String.valueOf(System.currentTimeMillis()));
            this.clog.log(le);
        }
    }

    public boolean start() {
        if (this.running) return false;
        this.interval = Long.parseLong(this.config.getStringParam("", "watchdogtimer"));
        this.timer = new Timer();
        this.timer.scheduleAtFixedRate(new PerfMonitorTask(this.region, this.agent, this.plugin, this.interval, this.clog, this.config), 500, this.interval);
        return true;
    }

    public void restart() {
        if (this.running) this.timer.cancel();
        this.running = false;
        start();
    }

    public boolean stop() {
        if (!this.running) return false;
        this.timer.cancel();
        this.running = false;
        return true;
    }
}