package com.bdang.facts;

import com.google.common.base.Preconditions;

import javax.annotation.concurrent.Immutable;

@Immutable
public class Fact {
    public static class Builder {
        private String subject;
        private String rel;
        private String object;

        public Builder subject(String subject) {
            this.subject = subject;
            return this;
        }

        public Builder rel(String rel) {
            this.rel = rel;
            return this;
        }

        public Builder object(String object) {
            this.object = object;
            return this;
        }

        public Fact build() {
            return new Fact(subject, rel, object);
        }
    }

    private final String subject;
    private final Relation rel;
    private final String object;

    private Fact(String subject, String rel, String object) {
        Preconditions.checkNotNull(subject);
        Preconditions.checkNotNull(rel);
        Preconditions.checkNotNull(object);

        this.subject = subject;
        this.rel = Relation.fromString(rel);
        this.object = object;
    }

    public String getSubject() {
        return subject;
    }

    public Relation getRel() {
        return rel;
    }

    public String getObject() {
        return object;
    }
}
