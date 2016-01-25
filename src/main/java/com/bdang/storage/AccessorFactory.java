package com.bdang.storage;

/**
 * Factory used to obtain an accessor.
 * Used to abstract away data storage tech from the rest of the application.
 * Can be replaced with Spring injection.
 */
public class AccessorFactory {

    private static TinkerGraphAccessor tinkerGraphAccessor;

    public static Accessor getAccessor(DBLocation dbLocation) {
        if (tinkerGraphAccessor == null) {
            tinkerGraphAccessor = new TinkerGraphAccessor(dbLocation);
        }
        return tinkerGraphAccessor;
    }
}
