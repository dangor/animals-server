package com.bdang.storage;

import com.bdang.facts.Fact;

import java.util.List;

public interface Accessor {
    String put(Fact fact);
    String delete(String id);
    Fact get(String id);
    List<String> find(Fact query);
    long count(Fact query);
}
