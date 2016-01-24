package com.bdang.storage;

import com.bdang.facts.Fact;
import com.bdang.facts.Relation;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import static org.hamcrest.CoreMatchers.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class TinkerGraphAccessorTest {

    private static final String NAME = "name";

    @Test
    public void testPut() throws Exception {
        Graph graph = TinkerGraph.open();
        GraphTraversalSource g = graph.traversal();
        Accessor accessor = new TinkerGraphAccessor(g);

        Fact fact = new Fact.Builder().subject("otter").rel(Relation.LIVES.toString()).object("river").build();

        String id1 = accessor.put(fact);

        assertTrue(g.V().has(NAME, "otter").hasNext());
        assertTrue(g.V().has(NAME, "river").hasNext());
        assertTrue(g.V().has(NAME, "otter").out(Relation.LIVES.toString()).has(NAME, "river").hasNext());

        String id2 = accessor.put(fact);

        assertThat(id2, equalTo(id1));
    }

    @Test
    public void testDelete() throws Exception {
        Graph graph = TinkerGraph.open();
        Vertex subject = graph.addVertex(NAME, "otter");
        Vertex object = graph.addVertex(NAME, "river");
        String id = subject.addEdge(Relation.LIVES.toString(), object).id().toString();

        GraphTraversalSource g = graph.traversal();
        Accessor accessor = new TinkerGraphAccessor(g);

        boolean deleted = accessor.delete(id);

        assertTrue(deleted);
        assertFalse(g.E().hasId(id).hasNext());

        deleted = accessor.delete("non-existent id");
        assertFalse(deleted);
    }

    @Test
    public void testGet() throws Exception {
        Graph graph = TinkerGraph.open();
        Vertex subject = graph.addVertex(NAME, "otter");
        Vertex object = graph.addVertex(NAME, "river");
        String id = subject.addEdge(Relation.LIVES.toString(), object).id().toString();

        GraphTraversalSource g = graph.traversal();
        Accessor accessor = new TinkerGraphAccessor(g);

        Fact fact = accessor.get(id);

        assertThat(fact.getSubject(), equalTo("otter"));
        assertThat(fact.getRel().toString(), equalTo(Relation.LIVES.toString()));
        assertThat(fact.getObject(), equalTo("river"));

        fact = accessor.get("non-existent id");
        assertNull(fact);
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
            vertex.addEdge(Relation.ISA.toString(), animal);
            vertex.addEdge(Relation.HAS.toString(), legs);
        }

        GraphTraversalSource g = graph.traversal();
        Accessor accessor = new TinkerGraphAccessor(g);

        Fact query = new Fact.Builder().subject("animal").rel("has").object("legs").build();
        List<String> names = accessor.find(query);

        assertThat(names.size(), is(3));
        assertThat(names, hasItems("otter", "fox", "moose"));
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
            vertex.addEdge(Relation.ISA.toString(), animal);
            vertex.addEdge(Relation.HAS.toString(), legs);
        }

        GraphTraversalSource g = graph.traversal();
        Accessor accessor = new TinkerGraphAccessor(g);

        Fact query = new Fact.Builder().subject("animal").rel("has").object("legs").build();
        long count = accessor.count(query);

        assertThat(count, is(3L));
    }
}