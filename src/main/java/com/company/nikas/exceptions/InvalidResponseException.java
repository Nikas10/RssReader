package com.company.nikas.exceptions;

/**
 * This exception is thrown in case server returns bad output.
 */
public class InvalidResponseException extends RuntimeException {

    public InvalidResponseException(String message) {
        super(message);
    }

    public InvalidResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}
