package com.company.nikas.system.processing.impl;

import com.company.nikas.config.AppConfiguration;
import com.company.nikas.exceptions.RssParserException;
import com.company.nikas.model.RssConfiguration;
import com.company.nikas.system.processing.RssProcessor;
import com.sun.syndication.feed.WireFeed;
import com.sun.syndication.feed.rss.Item;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.WireFeedInput;
import lombok.extern.slf4j.Slf4j;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

@Slf4j
public class RomeRssProcessor implements RssProcessor {

    static Logger logger = Logger.getLogger(RomeRssProcessor.class);
    static {
        logger.setLevel(Level.INFO);
    }

    private RssConfiguration rssConfiguration;
    private Map<String, Object> parseResult;

    public RomeRssProcessor() {
    }

    @Override
    public Map<String, Object> parseFeed(String feedId, String content) throws RssParserException {
        rssConfiguration = AppConfiguration.getRssFeeds().get(feedId);
        parseResult = new HashMap<>();
        WireFeedInput syndFeedInput = new WireFeedInput();
        WireFeed feed;
        try {
            feed = syndFeedInput.build(new StringReader(content));
        } catch (FeedException e) {
            log.error("Error occured while parsing feed contents,", e);
            throw new RssParserException("Unable to parse RSS feed!", e);
        }
        return buildMappedRss(feed);
    }

    private Map<String, Object> buildMappedRss(WireFeed feed) {
        Map<String, String> allowedTags = rssConfiguration.getActiveTags();
        allowedTags.forEach((key, commandToInvoke) -> {
            try {
                Method toInvoke = feed.getClass().getMethod(commandToInvoke, null);
                parseResult.put(key, toInvoke.invoke(feed, null));
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                log.error("Invalid method invocation, ignoring element.");
            }
        });
        return parseResult;
    }
}
