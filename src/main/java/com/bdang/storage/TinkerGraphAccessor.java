package com.bdang.storage;

import com.bdang.facts.Fact;
import com.bdang.facts.Relation;
import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.tinkerpop.gremlin.process.traversal.Traverser;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.*;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.apache.tinkerpop.gremlin.util.config.YamlConfiguration;

import java.io.*;
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
                YamlConfiguration config = new YamlConfiguration();
                try {
                    config.load(new BufferedReader(new FileReader(remoteGremlinConfig)));
                } catch (ConfigurationException|FileNotFoundException e) {
                    System.err.println("Could not open file " + remoteGremlinConfig);
                    e.printStackTrace();
                    graph = TinkerGraph.open();
                    break;
                }
                graph = TinkerGraph.open(config);
                break;
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

    // Worst case: O(n) where n = edges with relationship from subject
    // Best case: O(1) if the edge already exists
    public String put(final Fact fact) {
        // Ensure both subject and object are existing vertices. If not, create them.
        boolean bothVerticesExist = true;

        GraphTraversal<Vertex, Vertex> tSubject = g.V().has(NAME, fact.getSubject());
        if (!tSubject.hasNext()) {
            tSubject = g.addV(NAME, fact.getSubject());
            bothVerticesExist = false;
        }
        Vertex vSubject = tSubject.next();

        GraphTraversal<Vertex, Vertex> tObject = g.V().has(NAME, fact.getObject());
        if (!tObject.hasNext()) {
            tObject = g.addV(NAME, fact.getObject());
            bothVerticesExist = false;
        }
        Vertex vObject = tObject.next();

        // If both subject and object existed, then check for existing edge. If none, create one.
        if (bothVerticesExist) {
            Iterator<Edge> edges = vSubject.edges(Direction.OUT, fact.getRel());
            while (edges.hasNext()) {
                Edge edge = edges.next();
                if (edge.inVertex().id().equals(vObject.id())) {
                    return edge.id().toString();
                }
            }
        }

        return vSubject.addEdge(fact.getRel(), vObject).id().toString();
    }

    // O(1)
    public boolean delete(String id) {
        if (!g.E().hasId(id).hasNext()) {
            return false;
        }

        g.E().hasId(id).drop().iterate();
        return true;
    }

    // O(1)
    public Fact get(String id) {
        if (!g.E().hasId(id).hasNext()) {
            return null;
        }

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
                Iterator<Edge> edges = vertex.edges(Direction.OUT, query.getRel());
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

        return Collections.unmodifiableList(findResults);
    }

    // O(m * n) where m = concepts matching "isa [subject]" and n = edges with relationship from m concepts
    public long count(Fact query) {
        return g.V().has(NAME, query.getSubject()).in(Relation.ISA.toString()).out(query.getRel()).has(NAME, query.getObject()).count().next();
    }
}
