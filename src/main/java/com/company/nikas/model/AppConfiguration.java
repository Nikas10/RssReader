package com.company.nikas.model;

import lombok.Data;

import java.io.InputStream;
import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class AppConfiguration implements Serializable {

    private Map<String, RssConfiguration> rssFeeds;
    private Map<String, InputStream> activeConnections;

    public AppConfiguration() {
        rssFeeds = new ConcurrentHashMap<>();
        activeConnections = new ConcurrentHashMap<>();
    }

}
