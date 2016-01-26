package com.bdang.storage;

/**
 * Factory used to obtain an accessor.
 * Used to abstract away data storage tech from the rest of the application.
 * Can be replaced with Spring injection.
 */
public class AccessorFactory {

    private static Accessor inMemoryAccessor;
    private static Accessor externalAccessor;

    public static Accessor getAccessor() {
        return getAccessor(DBLocation.INMEMORY);
    }

    public static Accessor getAccessor(DBLocation dbLocation) {
        switch (dbLocation) {
            case EXTERNAL:
                if (externalAccessor == null) {
                    externalAccessor = new TinkerGraphAccessor(dbLocation);
                }
                return externalAccessor;
            case INMEMORY:
            default:
                if (inMemoryAccessor == null) {
                    inMemoryAccessor = new TinkerGraphAccessor(dbLocation);
                }
                return inMemoryAccessor;
        }
    }
}
