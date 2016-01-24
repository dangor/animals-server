package com.bdang.storage;

import com.bdang.facts.Fact;
import com.bdang.facts.Relation;
import com.google.common.annotations.VisibleForTesting;
import org.apache.tinkerpop.gremlin.process.traversal.Traverser;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.*;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;

import java.util.*;
import java.util.function.Predicate;

public class TinkerGraphAccessor implements Accessor {

    public enum Location {
        LOCAL,
        REMOTE
    }

    private static final String remoteGremlinConfig = "/Users/bdang/animals/dynamodb-titan-storage-backend/server/dynamodb-titan100-storage-backend-1.0.0-hadoop1/conf/remote.yaml";
    private static final String NAME = "name";
    private final GraphTraversalSource g;

    // Package visibility for factory
    TinkerGraphAccessor(Location location) {
        final Graph graph;
        switch (location) {
            case REMOTE:
                // graph = TinkerGraph.open(new org.apache.tinkerpop.gremlin.driver.YamlConfiguration(remoteGremlinConfig));
                g = null;
                return;
            case LOCAL :
            default:
                graph = TinkerGraph.open();
        }
        g = graph.traversal();
    }

    @VisibleForTesting
    TinkerGraphAccessor(GraphTraversalSource g) {
        this.g = g;
    }

    // O(n) where n = edges with relationship from subject
    public String put(final Fact fact) {
        // 1. Ensure both subject and object are existing vertices. If not, create them.
        // 2. Create an edge if it isn't already there
        final Vertex subject = getOrAddVertex(fact.getSubject());
        final Vertex object = getOrAddVertex(fact.getObject());

        Iterator<Edge> edges = subject.edges(Direction.OUT, fact.getRel().toString());
        while (edges.hasNext()) {
            Edge edge = edges.next();
            if (edge.inVertex().id().equals(object.id())) {
                return edge.id().toString();
            }
        }

        return subject.addEdge(fact.getRel().toString(), object).id().toString();
    }

    // O(1)
    private Vertex getOrAddVertex(String s) {
        GraphTraversal<Vertex, Vertex> t = g.V().has(NAME, s);
        return t.hasNext() ? t.next() : g.addV(NAME, s).next();
    }

    // O(1)
    public boolean delete(String id) {
        g.E().hasId(id).drop().iterate();

        return true;
    }

    // O(1)
    public Fact get(String id) {
        Fact.Builder builder = new Fact.Builder();

        builder.subject(g.E().hasId(id).outV().next().value(NAME).toString());
        builder.rel(g.E().hasId(id).next().label());
        builder.object(g.E().hasId(id).inV().next().value(NAME).toString());

        return builder.build();
    }

    // O(m * n) where m = concepts matching "isa [subject]" and n = edges with relationship from m concepts
    public List<String> find(final Fact query) {
        final List<String> findResults = new ArrayList<>();

        GraphTraversal<Vertex, Vertex> filter = g.V().has(NAME, query.getSubject()).in(Relation.ISA.toString()).filter(new Predicate<Traverser<Vertex>>() {
            @Override
            public boolean test(Traverser<Vertex> v) {
                Vertex vertex = v.get();
                Iterator<Edge> edges = vertex.edges(Direction.OUT, query.getRel().toString());
                while (edges.hasNext()) {
                    Edge edge = edges.next();
                    if (query.getObject().equals(edge.inVertex().value(NAME).toString())) {
                        return true;
                    }
                }
                return false;
            }
        });

        while(filter.hasNext()) {
            findResults.add(filter.next().value(NAME).toString());
        }

        return findResults;
    }

    // O(m * n) where m = concepts matching "isa [subject]" and n = edges with relationship from m concepts
    public long count(Fact query) {
        return g.V().has(NAME, query.getSubject()).in(Relation.ISA.toString()).out(query.getRel().toString()).has(NAME, query.getObject()).count().next();
    }
}
