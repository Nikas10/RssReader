package com.company.nikas.exceptions;

/**
 * Thrown when ROME's SyndFeedInput couldn't parse input XML file.
 */
public class RssParserException extends RuntimeException {
    public RssParserException(String message) {
        super(message);
    }

    public RssParserException(String message, Throwable cause) {
        super(message, cause);
    }
}
