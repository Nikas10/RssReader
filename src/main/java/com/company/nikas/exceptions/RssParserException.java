package com.company.nikas.exceptions;

public class RssParserException extends RuntimeException {
    public RssParserException(String message) {
        super(message);
    }

    public RssParserException(String message, Throwable cause) {
        super(message, cause);
    }
}
