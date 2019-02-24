package com.company.nikas.config;

import com.company.nikas.model.RssConfiguration;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class AppConfiguration implements Serializable {

    private static Map<String, RssConfiguration> rssFeeds;

    private static Map<String, String> rssTemplate;

    private static Map<String, String> atomTemplate;

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

    public static Map<String, String> getRssTemplate() {
        return rssTemplate;
    }

    public static void setRssTemplate(Map<String, String> template) {
        rssTemplate = template;
    }

    public static Map<String, String> getAtomTemplate() {
        return atomTemplate;
    }

    public static void setAtomTemplate(Map<String, String> template) {
        atomTemplate = template;
    }

    public AppConfiguration() {
        rssFeeds = new ConcurrentHashMap<>();
        activeConnections = new ConcurrentHashMap<>();
    }

}
