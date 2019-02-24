package com.company.nikas.system.processing.impl;

import com.company.nikas.config.AppConfiguration;
import com.company.nikas.exceptions.RssParserException;
import com.company.nikas.model.RssConfiguration;
import com.company.nikas.system.processing.RssProcessor;
import com.sun.syndication.feed.WireFeed;
import com.sun.syndication.feed.atom.Feed;
import com.sun.syndication.feed.rss.Channel;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.WireFeedInput;
import lombok.extern.slf4j.Slf4j;

import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;


@Slf4j
public class RomeRssProcessor implements RssProcessor {

    private RssConfiguration rssConfiguration;
    private List<Map<String, Object>> parseResult;

    public RomeRssProcessor() {
    }

    @Override
    public List<Map<String, Object>> parseFeed(String feedId, String content) throws RssParserException {
        rssConfiguration = AppConfiguration.getRssFeeds().get(feedId);
        parseResult = new ArrayList<>();
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

    private List<Map<String, Object>> buildMappedRss(WireFeed feed) {
        Map<String, String> allowedTags = rssConfiguration.getActiveTags();
        List list = getEntriesFromFeed(feed);
        Integer entryLimit = Optional.ofNullable(rssConfiguration.getElementsPerRequest())
                .orElse(list.size());
        for (Integer i = 0; i < entryLimit; i++) {
            Map<String, Object> parsedEntry = new HashMap<>();
            Object entry = list.get(i);
            allowedTags.forEach((key, commandToInvoke) -> {
                try {
                    Method toInvoke = entry.getClass().getMethod(commandToInvoke, null);
                    parsedEntry.put(key, toInvoke.invoke(entry, null));
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    log.error("Invalid method invocation, ignoring element.");
                }
            });
            parseResult.add(parsedEntry);
        }
        return parseResult;
    }

    private List getEntriesFromFeed(WireFeed feed) {
        List list;
        if (feed instanceof Channel) {
            list = ((Channel) feed).getItems();
        } else {
            list = ((Feed) feed).getEntries();
        }
        return list;
    }


}
