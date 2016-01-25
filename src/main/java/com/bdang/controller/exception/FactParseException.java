package com.bdang.controller.exception;

public class FactParseException extends RuntimeException {
    public FactParseException(String fact, Exception e) {
        super("Failed to parse fact: " + fact, e);
    }
}
