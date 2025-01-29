package com.gf.server.exceptions;

public class FailedSaveException extends RuntimeException {

    public FailedSaveException(String message) {
        super(message);
    }
}
