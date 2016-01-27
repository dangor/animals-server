package com.bdang.storage;

import com.bdang.facts.Fact;
import com.bdang.facts.Relation;
import com.bdang.storage.exception.UnregisteredConceptException;
import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.tinkerpop.gremlin.process.traversal.Traverser;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.apache.tinkerpop.gremlin.util.config.YamlConfiguration;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class TinkerGraphAccessor implements Accessor {

    public enum QueryInheritance {
        NONE,         // Crawl through "isa" relationship only once for the subject in query
        SUBJECT_ONLY, // Crawl through "isa" relationships for the subject in queries
        BOTH          // Crawl through "isa" relationships for both subject and object in queries
    }

    private static final String remoteGremlinConfig = "conf/remote.yaml";
    private static final String NAME = "name";
    private static final String GUID = "guid";
    private final GraphTraversalSource g;
    private final QueryInheritance inherit;

    // Package visibility for factory
    TinkerGraphAccessor(DBLocation dbLocation) {
        this(dbLocation, QueryInheritance.BOTH);
    }

    private TinkerGraphAccessor(DBLocation dbLocation, QueryInheritance inherit) {
        this.inherit = inherit;
        final TinkerGraph graph;
        switch (dbLocation) {
            case EXTERNAL:
                // Note: This section still needs work
                YamlConfiguration config = new YamlConfiguration();
                try {
                    config.load(new FileReader(remoteGremlinConfig));
                } catch (ConfigurationException|FileNotFoundException e) {
                    throw new RuntimeException("Unable to open file", e);
                }
                graph = TinkerGraph.open(config);
                break;
            case INMEMORY:
            default:
                graph = TinkerGraph.open();
        }
        graph.createIndex(NAME, Vertex.class);
        graph.createIndex(GUID, Edge.class);
        g = graph.traversal();
    }

    @VisibleForTesting
    TinkerGraphAccessor(GraphTraversalSource g) {
        this.g = g;
        inherit = QueryInheritance.BOTH;
    }

    // Worst case: O(n) where n = edges with relationship from subject
    // Best case: O(1) if the edge already exists
    @Override
    public String put(final Fact fact) {
        // Ensure both subject and object are existing vertices. If not, create them.
        boolean bothVerticesExist = true;

        GraphTraversal<Vertex, Vertex> tSubject = g.V().has(NAME, fact.getSubject()); // O(1)
        if (!tSubject.hasNext()) {
            tSubject = g.addV(NAME, fact.getSubject()); // O(1)
            bothVerticesExist = false;
        }
        Vertex vSubject = tSubject.next();

        GraphTraversal<Vertex, Vertex> tObject = g.V().has(NAME, fact.getObject()); // O(1)
        if (!tObject.hasNext()) {
            tObject = g.addV(NAME, fact.getObject()); // O(1)
            bothVerticesExist = false;
        }
        Vertex vObject = tObject.next();

        // If both subject and object existed, then check for existing edge. If none, create one.
        if (bothVerticesExist) {
            Iterator<Edge> edges = vSubject.edges(Direction.OUT, fact.getRel());
            while (edges.hasNext()) { // O(n)
                Edge edge = edges.next();
                if (edge.inVertex().id().equals(vObject.id())) { // O(1)
                    return edge.property(GUID).value().toString();
                }
            }
        }

        String guid = UUID.randomUUID().toString();
        vSubject.addEdge(fact.getRel(), vObject, GUID, guid); // O(1)
        return guid;
    }

    // O(1)
    @Override
    public boolean delete(String id) {
        if (!g.E().has(GUID, id).hasNext()) { // O(1) - edge property index has been created
            return false;
        }

        g.E().has(GUID, id).drop().iterate(); // O(1)
        return true;
    }

    // O(1)
    @Override
    public Fact get(String id) {
        if (!g.E().has(GUID, id).hasNext()) { // O(1) - edge property index has been created
            return null;
        }

        Edge e = g.E().has(GUID, id).next(); // O(1)

        Fact.Builder builder = new Fact.Builder();
        builder.subject(e.outVertex().value(NAME).toString()); // O(1)
        builder.rel(e.label()); // O(1)
        builder.object(e.inVertex().value(NAME).toString()); // O(1)

        return builder.build();
    }

    @Override
    public List<String> find(Fact query) throws UnregisteredConceptException {
        checkConceptsRegistered(query); // O(1)

        return inherit == QueryInheritance.NONE ? findNoInheritance(query) : findWithInheritance(query);
    }

    // O(m * n) where m = concepts matching "isa [subject]" and n = edges with relationship from m concepts
    private List<String> findNoInheritance(Fact query) {
        final List<String> findResults = new ArrayList<>();

        GraphTraversal<Vertex, Vertex> filter = g.V().has(NAME, query.getSubject()).in(Relation.ISA.toString()) // O(m)
                .filter(new QueryMatchFilter(query));

        while(filter.hasNext()) { // O(n)
            findResults.add(filter.next().value(NAME).toString());
        }

        return Collections.unmodifiableList(findResults);
    }

    // O(m) + O(n * k) where m = # of parents of subject and n = # of parents of object and k = # of related edges from n
    @VisibleForTesting
    List<String> findWithInheritance(Fact query) {
        // Step 1: Find all vertices which are parents of the query subject via "isa"
        Set<String> candidateSubjects = g.V().has(NAME, query.getSubject())
                .until(__.in(Relation.ISA.toString()).count().is(0L))
                .repeat(__.in(Relation.ISA.toString())).emit().dedup() // O(m)
                .toList().stream().map(V -> V.value(NAME).toString()).collect(Collectors.toSet());

        // Step 2: Find all object vertex, and include parents if QueryInheritance is both subject and object
        GraphTraversal<Vertex, Vertex> objectTraversal = g.V().has(NAME, query.getObject());
        if (inherit == QueryInheritance.BOTH) {
            objectTraversal = objectTraversal.until(__.in(Relation.ISA.toString()).count().is(0L))
                    .emit().repeat(__.in(Relation.ISA.toString())).dedup(); // O(n)
        }

        // Step 3: Traverse through edges with given relationship, then find intersection with candidate subject vertices
        Set<String> subjectsRelatedToObject = objectTraversal.in(query.getRel()).dedup() // O(n * k)
                .toList().stream().map(V -> V.value(NAME).toString()).collect(Collectors.toSet());

        candidateSubjects.retainAll(subjectsRelatedToObject); // linear

        return Collections.unmodifiableList(new ArrayList<>(candidateSubjects)); // linear
    }

    // Runtime complexity matches find()
    @Override
    public long count(Fact query) throws UnregisteredConceptException {
        checkConceptsRegistered(query); // O(1)

        return find(query).size();
    }

    // O(n) where n = # of concepts
    @Override
    public boolean deleteAll() {
        g.V().drop().iterate();
        return !g.V().hasNext();
    }

    // O(1)
    private void checkConceptsRegistered(Fact query) throws UnregisteredConceptException {
        if (!g.V().has(NAME, query.getSubject()).hasNext()) { // O(1)
            throw new UnregisteredConceptException(query.getSubject());
        } else if (!g.V().has(NAME, query.getObject()).hasNext()) { // O(1)
            throw new UnregisteredConceptException(query.getObject());
        }
    }

    /**
     * Predicate returning true if a vertex has the specified edge to another vertex
     */
    private static class QueryMatchFilter implements Predicate<Traverser<Vertex>> {
        private final Fact query;

        public QueryMatchFilter(Fact query) {
            this.query = query;
        }

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
    }
}
