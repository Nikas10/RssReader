package com.company.nikas.config;

import com.company.nikas.model.RssConfiguration;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * Class, responsible for storing application configuration data:
 * current feed subscription, current active transactions.
 */
@Data
public class AppConfiguration implements Serializable {

    /** Stores feed subscription data*/
    private static Map<String, RssConfiguration> rssFeeds;

    /** Stores mapping - rss feed tag: object getter (used during parse process)*/
    private static Map<String, String> syndTemplate;

    /** Stores information about received feed data for writing in file*/
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

    public static Map<String, String> getSyndTemplate() {
        return syndTemplate;
    }

    public static void setSyndTemplate(Map<String, String> template) { syndTemplate = template; }

}
