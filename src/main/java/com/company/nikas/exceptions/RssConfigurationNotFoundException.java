package com.company.nikas.exceptions;

/**
 * This exception is thrown when ActiveStreamMonitor finds received subscription data,
 * but does not find find subscription itself
 */
public class RssConfigurationNotFoundException extends RuntimeException {

    public RssConfigurationNotFoundException(String message) {
        super(message);
    }

    public RssConfigurationNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
