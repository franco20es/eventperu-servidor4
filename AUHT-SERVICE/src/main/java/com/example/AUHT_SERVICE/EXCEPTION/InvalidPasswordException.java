package com.example.AUHT_SERVICE.EXCEPTION;

public class InvalidPasswordException extends RuntimeException {
    public InvalidPasswordException(String message) {
        super(message);
    }
}
