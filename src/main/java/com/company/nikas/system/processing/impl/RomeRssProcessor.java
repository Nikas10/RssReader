package com.company.nikas.system.processing.impl;

import com.company.nikas.config.AppConfiguration;
import com.company.nikas.exceptions.RssParserException;
import com.company.nikas.model.RssConfiguration;
import com.company.nikas.system.processing.RssProcessor;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import lombok.extern.slf4j.Slf4j;

import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;


/**
 * ROME inplementation of RssProcessor interface.
 */
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
        SyndFeedInput wireFeedInput = new SyndFeedInput();
        SyndFeed feed;
        try {
            feed = wireFeedInput.build(new StringReader(content));
        } catch (FeedException e) {
            log.error("Error occured while parsing feed contents,", e);
            throw new RssParserException("Unable to parse RSS feed!", e);
        }
        return buildMappedRss(feed);
    }

    /**
     * Creates mapped collection based on ROME SyndFeed object.
     * @param feed SyndFeed representation of RSS XML data.
     * @return mapped collection, representing feed XML data.
     */
    private List<Map<String, Object>> buildMappedRss(SyndFeed feed) {
        Map<String, String> allowedTags = filterTemplate();
        List list = feed.getEntries();
        Integer entryLimit = Optional.ofNullable(rssConfiguration.getElementsPerRequest())
                .orElse(list.size());
        if (entryLimit > list.size() || entryLimit < 1) {
            entryLimit = list.size();
        }
        for (Integer i = 0; i < entryLimit; i++) {
            Map<String, Object> parsedEntry = new HashMap<>();
            Object entry = list.get(i);
            allowedTags.forEach((key, commandToInvoke) -> {
                try {
                    Method toInvoke = entry.getClass().getMethod(commandToInvoke, null);
                    parsedEntry.put(key, toInvoke.invoke(entry, null));
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    log.error("Invalid method invocation, ignoring element.", e);
                }
            });
            parseResult.add(parsedEntry);
        }
        return parseResult;
    }

    /**
     * Manages, which tags will be included in mapped data representation
     * @return Map, containg tag name as a key and SyndFeed getter as a value.
     */
    private Map<String, String> filterTemplate() {
        Map<String, String> template = AppConfiguration.getSyndTemplate();
        Set<String> tags = Optional.ofNullable(rssConfiguration.getActiveTags())
                .orElse(AppConfiguration.getSyndTemplate().keySet());
        return template.entrySet()
                .stream()
                .filter(entry -> tags.contains(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

}
