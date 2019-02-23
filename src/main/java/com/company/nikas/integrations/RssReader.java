package com.company.nikas.integrations;

import java.io.InputStream;

public interface RssReader {

    /**
     * Get RSS input from given String URL
     * @param url Web URL
     * @return Response from Rss server
     */
    public InputStream getRssFeed(String url);
}
