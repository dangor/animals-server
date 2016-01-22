package com.bdang.storage;

/**
 * Factory used to obtain an accessor.
 * Used to abstract away data storage tech from the rest of the application.
 * Can be replaced with Spring injection.
 */
public class AccessorFactory {

    private static GremlinAccessor gremlinAccessor;

    public static Accessor getAccessor() {
        if (gremlinAccessor == null) {
            gremlinAccessor = new GremlinAccessor();
        }
        return gremlinAccessor;
    }
}
