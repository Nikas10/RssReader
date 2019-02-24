package com.company.nikas.config;

import com.company.nikas.model.RssConfiguration;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class AppConfiguration implements Serializable {

    private static Map<String, RssConfiguration> rssFeeds;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private static Map<String, String> activeConnections;

    public static Map<String, RssConfiguration> getRssFeeds() {
        return rssFeeds;
    }

    public static void setRssFeeds(Map<String, RssConfiguration> feeds) {
        rssFeeds = feeds;
    }

    public static Map<String, String> getActiveConnections() {
        return activeConnections;
    }

    public static void setActiveConnections(Map<String, String> connections) {
        activeConnections = connections;
    }

    public AppConfiguration() {
        rssFeeds = new ConcurrentHashMap<>();
        activeConnections = new ConcurrentHashMap<>();
    }

}
