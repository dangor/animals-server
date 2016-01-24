package com.bdang.facts;

import com.google.common.base.Objects;
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
    private final String rel;
    private final String object;

    private Fact(String subject, String rel, String object) {
        Preconditions.checkNotNull(subject);
        Preconditions.checkNotNull(rel);
        Preconditions.checkNotNull(object);

        // Check relation whitelist
        Relation.fromString(rel);

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Fact fact = (Fact) o;
        return Objects.equal(subject, fact.subject) &&
                Objects.equal(rel, fact.rel) &&
                Objects.equal(object, fact.object);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(subject, rel, object);
    }
}
