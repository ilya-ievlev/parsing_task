package com.ievlev.dataox.exception;

public class IncorrectConnectionException extends RuntimeException{
    public IncorrectConnectionException() {
    }

    public IncorrectConnectionException(String message) {
        super(message);
    }

    public IncorrectConnectionException(String message, Throwable cause) {
        super(message, cause);
    }

    public IncorrectConnectionException(Throwable cause) {
        super(cause);
    }
}
