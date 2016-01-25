package com.bdang.controller.exception;

public class FactIdNotFoundException extends RuntimeException {
    public FactIdNotFoundException(String id) {
        super("Did not find a fact with id " + id);
    }
}
