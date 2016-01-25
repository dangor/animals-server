package com.bdang.storage;

import com.bdang.facts.Fact;
import com.bdang.storage.exception.UnregisteredConceptException;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class TinkerGraphAccessorTest {

    private static final String NAME = "name";
    private static final String GUID = "guid";

    @Test
    public void testPut() throws Exception {
        Graph graph = TinkerGraph.open();
        GraphTraversalSource g = graph.traversal();
        Accessor accessor = new TinkerGraphAccessor(g);

        Fact fact = new Fact.Builder().subject("otter").rel("lives").object("river").build();

        String id1 = accessor.put(fact);

        assertThat(g.V().has(NAME, "otter").count().next(), is(1L));
        assertThat(g.V().has(NAME, "river").count().next(), is(1L));
        assertThat(g.V().has(NAME, "otter").out("lives").has(NAME, "river").count().next(), is(1L));

        String id2 = accessor.put(fact);

        assertThat(id2, equalTo(id1));
    }

    @Test
    public void testDelete() throws Exception {
        Graph graph = TinkerGraph.open();
        Vertex subject = graph.addVertex(NAME, "otter");
        Vertex object = graph.addVertex(NAME, "river");
        String id = "1234";
        subject.addEdge("lives", object, GUID, id);

        GraphTraversalSource g = graph.traversal();
        Accessor accessor = new TinkerGraphAccessor(g);

        boolean deleted = accessor.delete(id);

        assertThat(deleted, is(true));
        assertThat(g.E().has(GUID, id).count().next(), is(0L));

        deleted = accessor.delete("non-existent id");
        assertThat(deleted, is(false));
    }

    @Test
    public void testGet() throws Exception {
        Graph graph = TinkerGraph.open();
        Vertex subject = graph.addVertex(NAME, "otter");
        Vertex object = graph.addVertex(NAME, "river");
        String id = "1234";
        subject.addEdge("lives", object, GUID, id);

        GraphTraversalSource g = graph.traversal();
        Accessor accessor = new TinkerGraphAccessor(g);

        Fact fact = accessor.get(id);

        assertThat(fact.getSubject(), equalTo("otter"));
        assertThat(fact.getRel(), equalTo("lives"));
        assertThat(fact.getObject(), equalTo("river"));

        fact = accessor.get("non-existent id");
        assertThat(fact, nullValue());
    }

    @Test
    public void testFind() throws Exception {
        Graph graph = TinkerGraph.open();
        
        List<Vertex> animals = new ArrayList<>(3);
        animals.add(graph.addVertex(NAME, "otter"));
        animals.add(graph.addVertex(NAME, "fox"));
        animals.add(graph.addVertex(NAME, "moose"));
        Vertex legs = graph.addVertex(NAME, "legs");
        Vertex animal = graph.addVertex(NAME, "animal");
        
        for (Vertex vertex : animals) {
            vertex.addEdge("isa", animal);
            vertex.addEdge("has", legs);
        }

        GraphTraversalSource g = graph.traversal();
        Accessor accessor = new TinkerGraphAccessor(g);

        Fact query = new Fact.Builder().subject("animal").rel("has").object("legs").build();
        List<String> names = accessor.find(query);

        assertThat(names.size(), is(3));
        assertThat(names, hasItems("otter", "fox", "moose"));
    }

    @Test
    public void testFindUnregisteredConcept() throws Exception {
        Graph graph = TinkerGraph.open();

        GraphTraversalSource g = graph.traversal();
        Accessor accessor = new TinkerGraphAccessor(g);

        Fact query = new Fact.Builder().subject("animal").rel("has").object("legs").build();

        try {
            accessor.find(query);
            fail("Expected an exception to be thrown, but didn't catch one.");
        } catch (Exception e) {
            assertThat(e, instanceOf(UnregisteredConceptException.class));
        }
    }

    @Test
    public void testCount() throws Exception {
        Graph graph = TinkerGraph.open();

        List<Vertex> animals = new ArrayList<>(3);
        animals.add(graph.addVertex(NAME, "otter"));
        animals.add(graph.addVertex(NAME, "fox"));
        animals.add(graph.addVertex(NAME, "moose"));
        Vertex legs = graph.addVertex(NAME, "legs");
        Vertex animal = graph.addVertex(NAME, "animal");

        for (Vertex vertex : animals) {
            vertex.addEdge("isa", animal);
            vertex.addEdge("has", legs);
        }

        GraphTraversalSource g = graph.traversal();
        Accessor accessor = new TinkerGraphAccessor(g);

        Fact query = new Fact.Builder().subject("animal").rel("has").object("legs").build();
        long count = accessor.count(query);

        assertThat(count, is(3L));
    }

    @Test
    public void testCountUnregisteredConcept() throws Exception {
        Graph graph = TinkerGraph.open();

        GraphTraversalSource g = graph.traversal();
        Accessor accessor = new TinkerGraphAccessor(g);

        Fact query = new Fact.Builder().subject("animal").rel("has").object("legs").build();

        try {
            accessor.count(query);
            fail("Expected an exception to be thrown, but didn't catch one.");
        } catch (Exception e) {
            assertThat(e, instanceOf(UnregisteredConceptException.class));
        }
    }
}