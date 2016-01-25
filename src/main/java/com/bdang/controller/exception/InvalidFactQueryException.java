package com.bdang.controller.exception;

import com.bdang.facts.Fact;

public class InvalidFactQueryException extends RuntimeException {
    public InvalidFactQueryException(Fact query, Exception e) {
        super("Could not find information related to query: " + query, e);
    }
}
