package com.company.nikas.system.processing;

import com.company.nikas.exceptions.RssParserException;

import java.util.Map;

public interface RssProcessor {

    public Map<String, Object> parseFeed(String feedId, String content)
            throws RssParserException;

}
