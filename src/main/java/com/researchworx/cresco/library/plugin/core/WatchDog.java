package com.researchworx.cresco.library.plugin.core;

import com.researchworx.cresco.library.messaging.MsgEvent;
import com.researchworx.cresco.library.plugin.utilities.CLogger;
import com.researchworx.cresco.library.plugin.utilities.Config;

import java.util.Timer;
import java.util.TimerTask;

public class WatchDog {
    /** Timer control object */
    private Timer timer;
    /** Control object */
    private boolean running = false;
    /** Start time of the WatchDog timer */
    private long startTS;
    /** Region to report from */
    private String region;
    /** Agent to report from */
    private String agent;
    /** Plugin ID to report from */
    private String pluginID;
    /** Logger instance */
    private CLogger logger;
    /** Configuration instance */
    private Config config;

    /**
     * Constructor
     * @param region        Region to report from
     * @param agent         Agent to report from
     * @param pluginID      Plugin ID to report from
     * @param logger        Logger instance
     * @param config        Configuration instance
     */
    public WatchDog(String region, String agent, String pluginID, CLogger logger, Config config) {
        this.region = region;
        this.agent = agent;
        this.pluginID = pluginID;
        this.logger = logger;
        this.config = config;
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
        restart();
    }

    /**
     * Updates the parameters of this WatchDog instance
     * @param region        New Region to report from
     * @param agent         New Agent to report from
     * @param pluginID      New Plugin ID to report from
     * @param logger        New CLogger to use for errors
     * @param config        New Config to use for settings
     */
    public void update(String region, String agent, String pluginID, CLogger logger, Config config) {
        setLogger(logger);
        setConfig(config);
        update(region, agent, pluginID);
    }

    /**
     * Starts this WatchDog instance
     * @return              This instance
     */
    public WatchDog start() {
        if (running) return this;
        Long interval = config.getLongParam("watchdogtimer", 5000L);
        startTS = System.currentTimeMillis();
        MsgEvent enabled = new MsgEvent(MsgEvent.Type.CONFIG, region, null, null, "WatchDog timer set to " + interval + " milliseconds");
        enabled.setParam("src_region", region);
        enabled.setParam("src_agent", agent);
        enabled.setParam("src_plugin", pluginID);
        enabled.setParam("dst_region", region);
        enabled.setParam("dst_agent", agent);
        enabled.setParam("watchdogtimer", String.valueOf(interval));
        enabled.setParam("config", config.getConfigAsJSON());
        enabled.setParam("action", "enable");
        logger.log(enabled);
        timer = new Timer();
        timer.scheduleAtFixedRate(new WatchDogTask(region, agent, pluginID, logger), 500, interval);
        running = true;
        return this;
    }

    /**
     * Restarts this WatchDog instance
     * @return              This instance
     */
    public WatchDog restart() {
        if (running) timer.cancel();
        this.running = false;
        return start();
    }

    /**
     * Stops this WatchDog instance
     * @return              Whether the instance was stopped
     */
    public boolean stop() {
        if (!running) return false;
        timer.cancel();
        running = false;
        MsgEvent disabled = new MsgEvent(MsgEvent.Type.CONFIG, region, null, null, "WatchDog timer is shutting down");
        disabled.setParam("src_region", region);
        disabled.setParam("src_agent", agent);
        disabled.setParam("src_plugin", pluginID);
        disabled.setParam("dst_region", region);
        disabled.setParam("dst_agent", agent);
        disabled.setParam("action", "disable");
        logger.log(disabled);
        return true;
    }

    /**
     * Tick of the WatchDog timer
     */
    private class WatchDogTask extends TimerTask {
        /** Region to report from */
        private String region;
        /** Agent to report from */
        private String agent;
        /** Plugin ID to report from */
        private String pluginID;
        /** Logger channel */
        private CLogger logger;

        /**
         * Constructor
         * @param region        Region to report from
         * @param agent         Agent to report from
         * @param pluginID      Plugin ID to report from
         * @param logger        Logger channel
         */
        WatchDogTask(String region, String agent, String pluginID, CLogger logger) {
            this.region = region;
            this.agent = agent;
            this.pluginID = pluginID;
            this.logger = logger;
        }

        /**
         * Tick
         */
        public void run() {
            MsgEvent tick = new MsgEvent(MsgEvent.Type.WATCHDOG, region, null, null, "WatchDog timer tick.");
            tick.setParam("src_region", this.region);
            tick.setParam("src_agent", this.agent);
            tick.setParam("src_plugin", this.pluginID);
            tick.setParam("dst_region", this.region);
            tick.setParam("dst_agent", this.agent);
            tick.setParam("runtime", String.valueOf(System.currentTimeMillis() - startTS));
            tick.setParam("timestamp", String.valueOf(System.currentTimeMillis()));
            this.logger.log(tick);
        }
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

    /**
     * Configuration getter
     * @return          Current configuration
     */
    public Config getConfig() {
        return config;
    }

    /**
     * Configuration setter
     * @param config    New configuration
     */
    public void setConfig(Config config) {
        this.config = config;
    }
}
