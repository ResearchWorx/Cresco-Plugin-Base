package com.researchworx.cresco.plugins.base.core;

import com.researchworx.cresco.plugins.base.messaging.MsgEvent;
import com.researchworx.cresco.plugins.base.messaging.MsgEventType;
import com.researchworx.cresco.plugins.base.utilities.Clogger;

import java.util.Timer;
import java.util.TimerTask;

public class WatchDog {
    private Timer timer;
    private boolean running = false;
    private long interval;
    private long startTS;

    private String region;
    private String agent;
    private String plugin;
    private Clogger clog;
    private Config config;

    public WatchDog(String region, String agent, String plugin, Clogger clog, Config config) {
        this.region = region;
        this.agent = agent;
        this.plugin = plugin;
        this.clog = clog;
        this.config = config;

        this.interval = Long.parseLong(config.getStringParam("", "watchdogtimer"));
        startTS = System.currentTimeMillis();

        MsgEvent initial = new MsgEvent(MsgEventType.INFO, this.region, null, null, "WatchDog timer set to " + this.interval + " milliseconds");
        initial.setParam("src_region", this.region);
        initial.setParam("src_agent", this.agent);
        initial.setParam("src_plugin", this.plugin);
        initial.setParam("dst_region", this.region);
        this.clog.log(initial);

        start();
    }

    private class WatchDogTask extends TimerTask {
        private String region;
        private String agent;
        private String plugin;
        private Long interval;
        private Clogger clog;

        WatchDogTask(String region, String agent, String plugin, Long interval, Clogger clog) {
            this.region = region;
            this.agent = agent;
            this.plugin = plugin;
            this.interval = interval;
            this.clog = clog;
        }

        public void run() {
            long runTime = System.currentTimeMillis() - startTS;
            MsgEvent tick = new MsgEvent(MsgEventType.WATCHDOG, this.region, null, null, "WatchDog timer set to " + this.interval + " milliseconds");
            tick.setParam("src_region", this.region);
            tick.setParam("src_agent", this.agent);
            tick.setParam("src_plugin", this.plugin);
            tick.setParam("dst_region", this.region);
            tick.setParam("runtime", String.valueOf(runTime));
            tick.setParam("timestamp", String.valueOf(System.currentTimeMillis()));
            this.clog.log(tick);
        }
    }

    public boolean start() {
        if (this.running) return false;
        this.interval = Long.parseLong(this.config.getStringParam("", "watchdogtimer"));
        this.timer = new Timer();
        this.timer.scheduleAtFixedRate(new WatchDogTask(this.region, this.agent, this.plugin, this.interval, this.clog), 500, this.interval);
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
