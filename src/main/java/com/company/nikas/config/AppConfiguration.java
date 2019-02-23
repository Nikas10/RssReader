package com.company.nikas.config;

import com.company.nikas.model.RssConfiguration;
import lombok.Data;

import java.io.InputStream;
import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class AppConfiguration implements Serializable {

    private Map<String, RssConfiguration> rssFeeds;
    private Map<String, String> activeConnections;

    public AppConfiguration() {
        rssFeeds = new ConcurrentHashMap<>();
        activeConnections = new ConcurrentHashMap<>();
    }

}
