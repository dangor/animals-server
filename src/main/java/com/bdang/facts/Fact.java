package com.bdang.facts;

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
    private final String rel;
    private final String object;

    private Fact(String subject, String rel, String object) {
        this.subject = subject;
        this.rel = rel;
        this.object = object;
    }

    public String getSubject() {
        return subject;
    }

    public String getRel() {
        return rel;
    }

    public String getObject() {
        return object;
    }
}
