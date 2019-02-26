package com.company.nikas.system.processing;

import com.company.nikas.exceptions.RssParserException;

import java.util.List;
import java.util.Map;

/**
 * Interface, defining methods for operating with RSS data (XML)
 */
public interface RssProcessor {

    /**
     * Formats received XML RSS feed information into serializable list
     * for further writing in file in JSON format.
     * @param feedId Feed name (for fetching feed metadata)
     * @param content Raw feed XML data
     * @return Collection representation of feed data.
     * @throws RssParserException in case raw feed data is damaged.
     */
    List<Map<String, Object>> parseFeed(String feedId, String content)
            throws RssParserException;

}
