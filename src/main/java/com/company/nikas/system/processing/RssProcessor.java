package com.company.nikas.system.processing;

import com.company.nikas.exceptions.RssParserException;

import java.util.List;
import java.util.Map;

public interface RssProcessor {

    List<Map<String, Object>> parseFeed(String feedId, String content)
            throws RssParserException;

}
