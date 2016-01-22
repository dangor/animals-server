package com.bdang.facts;

public enum Relation {
    HAS("has"),
    EATS("eats"),
    LIVES("lives"),
    ISA("isa");

    private final String string;

    Relation(String string) {
        this.string = string;
    }

    public static Relation fromString(String string) {
        for (Relation r : Relation.values()) {
            if (r.string.equals(string)) {
                return r;
            }
        }
        throw new IllegalArgumentException("Relationship " + string + " not supported");
    }

    @Override
    public String toString() {
        return string;
    }
}
