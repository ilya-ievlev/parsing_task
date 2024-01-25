package com.ievlev.dataox.exception;

public class GoogleSheetException extends RuntimeException{
    public GoogleSheetException() {
    }

    public GoogleSheetException(String message) {
        super(message);
    }

    public GoogleSheetException(String message, Throwable cause) {
        super(message, cause);
    }

    public GoogleSheetException(Throwable cause) {
        super(cause);
    }
}
