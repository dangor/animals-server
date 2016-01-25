package com.bdang.storage.exception;

public class UnregisteredConceptException extends Exception {
    public UnregisteredConceptException(String name) {
        super("Unregistered concept: " + name);
    }
}
