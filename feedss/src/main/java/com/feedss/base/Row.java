package com.feedss.base;

import java.util.HashMap;
import java.util.Set;

/**
 * 
 * @author shenbingtao
 * @createTime 2015年4月25日 下午11:29:12
 */
public class Row extends HashMap<String, Object>{

    public Row() {
        super();
    }

    public Row(Row row) {
        super(row);
    }

    private static final long serialVersionUID = -8941305097679426324L;

    public Set<String> getKeys() {
        return keySet();
    }

    public int getInt(String key, int defaultVal) {
        Object o = get(key);
        if (o == null) {
            return defaultVal;
        }
        try {
            return Integer.parseInt(o.toString());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    public long getLong(String key, long defaultVal) {
        Object o = get(key);
        if (o == null) {
            return defaultVal;
        }
        try {
            return Long.parseLong(o.toString());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    public String gets(String key, String defaultVal) {
        Object o = get(key);
        return o == null ? defaultVal : o.toString();
    }

    public String gets(String key) {
        Object o = get(key);
        return o == null ? null : o.toString();
    }

    public float getFloat(String key, float defaultVal) {
        Object o = get(key);
        if (o == null) {
            return defaultVal;
        }
        try {
            return Float.parseFloat(o.toString());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    public double getDouble(String key, double defaultVal) {
        Object o = get(key);
        if (o == null) {
            return defaultVal;
        }
        try {
            return Double.parseDouble(o.toString());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0;
    }

}
