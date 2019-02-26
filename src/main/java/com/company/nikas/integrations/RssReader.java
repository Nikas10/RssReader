package com.company.nikas.integrations;

/**
 * Interface, defining operation for getting RSS feed subscription data.
 */
public interface RssReader {

    /**
     * Get RSS input from given String URL
     * @param url Web URL
     * @return Response from Rss server
     */
    public String getRssFeed(String url);
}
