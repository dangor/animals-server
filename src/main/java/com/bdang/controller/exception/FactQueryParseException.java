package com.bdang.controller.exception;

import java.util.Arrays;

public class FactQueryParseException extends RuntimeException {
    public FactQueryParseException(Exception e, String... s) {
        super("Failed to parse query: " + Arrays.toString(s), e);
    }
}
