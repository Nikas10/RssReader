package com.company.nikas.exceptions;

public class RssConfigurationNotFoundException extends RuntimeException {

    public RssConfigurationNotFoundException(String message) {
        super(message);
    }

    public RssConfigurationNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
