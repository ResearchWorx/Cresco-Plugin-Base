package com.researchworx.cresco.library.plugin.utilities;

import org.apache.commons.configuration.SubnodeConfiguration;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Cresco configuration handler
 * @author V.K. Cody Bumgardner
 * @author Caylin Hickey
 * @since 0.1.0
 */
public class Config {
    /** Environmental Variable Prefix */
    private static final String ENV_PREFIX = "CRESCO_";
    /** Plugin configuration object */
    protected SubnodeConfiguration confObj;
    /**
     * Constructor
     * @param pluginConfig      Plugin configuration object
     */
    public Config (SubnodeConfiguration pluginConfig) {
        this.confObj = pluginConfig;
    }
    /**
     * Grab configuration entry as Boolean
     * @param param             Entry name to retrieve
     * @return                  Value of entry, null if missing
     */
    public Boolean getBooleanParam(String param) {
        String env = System.getenv(ENV_PREFIX + param);
        if (env != null) {
            if (env.toLowerCase().trim().equals("true") || env.trim().equals("1")) {
                return true;
            }
            if (env.toLowerCase().trim().equals("false") || env.trim().equals("0")) {
                return false;
            }
        }
        try {
            return this.confObj.getBoolean(param);
        } catch (NoSuchElementException e) {
            return null;
        }
    }
    /**
     * Grab configuration entry as Boolean
     * @param param             Entry name to retrieve
     * @param ifNull            Default value to return on error
     * @return                  Value of entry, ifNull value on error
     */
    public Boolean getBooleanParam(String param, Boolean ifNull) {
        Boolean ret = getBooleanParam(param);
        if (ret != null)
            return ret;
        return ifNull;
    }
    /**
     * Grab configuration entry as Double
     * @param param             Entry name to retrieve
     * @return                  Value of entry, null if missing
     */
    public Double getDoubleParam(String param) {
        try {
            return Double.parseDouble(System.getenv(ENV_PREFIX + param));
        } catch (NumberFormatException nfe) {
            try {
                return this.confObj.getDouble(param);
            } catch (NoSuchElementException nsee) {
                return null;
            }
        }
    }
    /**
     * Grab configuration entry as Double
     * @param param             Entry name to retrieve
     * @param ifNull            Default value to return on error
     * @return                  Value of entry, ifNull value on error
     */
    public Double getDoubleParam(String param, Double ifNull) {
        Double ret = getDoubleParam(param);
        if (ret != null)
            return ret;
        return ifNull;
    }
    /**
     * Grab configuration entry as Integer
     * @param param             Entry name to retrieve
     * @return                  Value of entry, null if missing
     */
    public Integer getIntegerParam(String param) {
        try {
            return Integer.parseInt(System.getenv(ENV_PREFIX + param));
        } catch (NumberFormatException nfe) {
            try {
                return this.confObj.getInt(param);
            } catch (NoSuchElementException nsee) {
                return null;
            }
        }
    }
    /**
     * Grab configuration entry as Integer
     * @param param             Entry name to retrieve
     * @param ifNull            Default value to return on error
     * @return                  Value of entry, ifNull value on error
     */
    public Integer getIntegerParam(String param, Integer ifNull) {
        Integer ret = getIntegerParam(param);
        if (ret != null)
            return ret;
        return ifNull;
    }
    /**
     * Grab configuration entry as Long
     * @param param             Entry name to retrieve
     * @return                  Value of entry, null if missing
     */
    public Long getLongParam(String param) {
        try {
            return Long.parseLong(System.getenv(ENV_PREFIX + param));
        } catch (NumberFormatException nfe) {
            try {
                return this.confObj.getLong(param);
            } catch (NoSuchElementException nsee) {
                return null;
            }
        }
    }
    /**
     * Grab configuration entry as Long
     * @param param             Entry name to retrieve
     * @param ifNull            Default value to return on error
     * @return                  Value of entry, ifNull value on error
     */
    public Long getLongParam(String param, Long ifNull) {
        Long ret = getLongParam(param);
        if (ret != null)
            return ret;
        return ifNull;
    }
    /**
     * Grab configuration entry as String
     * @param param             Entry name to retrieve
     * @return                  Value of entry, null if missing
     */
    public String getStringParam(String param) {
        String env = System.getenv(ENV_PREFIX + param);
        if (env != null)
            return env;
        try {
            return this.confObj.getString(param);
        } catch (NoSuchElementException e) {
            return null;
        }
    }
    /**
     * Grab configuration entry as String
     * @param param             Entry name to retrieve
     * @param ifNull            Default value to return on error
     * @return                  Value of entry, ifNull value on error
     */
    public String getStringParam(String param, String ifNull) {
        String ret = getStringParam(param);
        if (ret != null)
            return ret;
        return ifNull;
    }
    /**
     * Returns the underlying configuration object
     * @return                  The underlying configuration object
     */
    public SubnodeConfiguration getConfig() {
        return this.confObj;
    }
    /**
     * Returns a JSON representation of the configuration object
     * @return                  JSONified configuration object
     */
    public String getConfigAsJSON() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        Iterator it = this.confObj.getKeys();
        while (it.hasNext()) {
            String key = (String)it.next();
            String value = this.confObj.getString(key);
            sb.append("\"");
            sb.append(key);
            sb.append("\":\"");
            sb.append(value);
            sb.append("\",");
        }
        if (sb.lastIndexOf(",") > -1)
            sb.deleteCharAt(sb.lastIndexOf(","));
        sb.append("}");
        return sb.toString();
    }
}
