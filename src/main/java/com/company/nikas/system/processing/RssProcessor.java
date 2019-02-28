package com.company.nikas.system.processing;

import com.company.nikas.exceptions.RssParserException;

import java.io.File;
import java.io.IOException;
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

    /**
     * Writes parsed feed data to a file.
     * @param feed Feed name
     * @param file File object, representing file to write to
     * @param content Mapped feed content
     * @throws IOException in case writing to file was unsuccessful.
     */
    public void writeToFile(String feed, File file, Map<String, Object> content)
            throws IOException;

}
