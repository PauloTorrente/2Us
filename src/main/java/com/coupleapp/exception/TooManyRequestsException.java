package com.coupleapp.exception;

// Thrown when a client exceeds their rate limit.
// Returns HTTP 429 Too Many Requests.
public class TooManyRequestsException extends RuntimeException {
    public TooManyRequestsException(String message) {
        super(message);
    }
}
