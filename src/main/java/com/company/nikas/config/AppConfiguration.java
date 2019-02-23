package com.company.nikas.config;

import com.company.nikas.model.RssConfiguration;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class AppConfiguration implements Serializable {

    private static Map<String, RssConfiguration> rssFeeds;
    private static Map<String, String> activeConnections;

    public static Map<String, RssConfiguration> getRssFeeds() {
        return rssFeeds;
    }

    public static Map<String, String> getActiveConnections() {
        return activeConnections;
    }

    public AppConfiguration() {
        rssFeeds = new ConcurrentHashMap<>();
        activeConnections = new ConcurrentHashMap<>();
    }

}
