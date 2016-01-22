package com.bdang.storage;

import com.bdang.facts.Fact;

import java.util.List;

public class GremlinAccessor implements Accessor {

    // Package visibility
    GremlinAccessor() {}

    public String put(Fact fact) {
        return null;
    }

    public String delete(String id) {
        return null;
    }

    public Fact get(String id) {
        return null;
    }

    public List<String> find(Fact query) {
        return null;
    }

    public long count(Fact query) {
        return 0;
    }
}
