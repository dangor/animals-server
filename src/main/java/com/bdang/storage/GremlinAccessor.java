package com.bdang.storage;

import com.bdang.facts.Fact;
import org.apache.tinkerpop.gremlin.driver.Client;
import org.apache.tinkerpop.gremlin.driver.Cluster;
import org.apache.tinkerpop.gremlin.driver.Result;
import org.apache.tinkerpop.gremlin.driver.ResultSet;
import org.apache.tinkerpop.gremlin.structure.Edge;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GremlinAccessor implements Accessor {

    // Package visibility for factory
    GremlinAccessor() {}

    public String put(Fact fact) {
        String id = null;

        Cluster cluster = Cluster.open();
        Client client = cluster.connect();

        try {
            // 1. Ensure both subject and object are existing vertices. If not, create them.
            // 2. Create an edge if it isn't already there
            Map<String,Object> params = new HashMap<>();
            params.put("?", fact.getSubject());
            Result result = client.submit("g.V().has('name','?')", params).one();
            if (result == null) {
                client.submit("g.addV('name','?')", params);
            }

            params.put("?", fact.getObject());
            result = client.submit("g.V().has('name','?')", params).one();
            if (result == null) {
                client.submit("g.addV('name','?')", params);
            }

            result = client.submit("g.E().hasLabel('" + fact.getRel().toString()
                    + "').and(outV().has('name','" + fact.getSubject()
                    + "'),inV().has('name','" + fact.getObject()
                    + "'))", params).one();
            if (result == null) {
                result = client.submit("g.withSideEffect('a',g.V().has('name','?')).V().has('name','?').addOutE('?','a')").one();
            }

            client.submit("g.commit()");

            id = result.getEdge().id().toString();
        } finally {
            client.close();
            cluster.close();
        }

        return id;
    }

    public boolean delete(String id) {
        boolean deleted = false;

        Cluster cluster = Cluster.open();
        Client client = cluster.connect();

        try {
            Map<String,Object> params = new HashMap<>();
            params.put("?", id);
            ResultSet results = client.submit("g.E(?)", params);
            Result result = results.one();
            if (result != null) {
                client.submit("g.E().hasId(?).drop()", params);
                client.submit("g.commit()");
                deleted = true;
            }
        } finally {
            client.close();
            cluster.close();
        }

        return deleted;
    }

    public Fact get(String id) {
        Fact fact = null;

        Cluster cluster = Cluster.open();
        Client client = cluster.connect();

        try {
            Map<String,Object> params = new HashMap<>();
            params.put("?", id);
            ResultSet results = client.submit("g.E(?)", params);
            Result result = results.one();
            if (result != null) {
                Edge edge = result.getEdge();
                fact = new Fact.Builder()
                        .subject(edge.inVertex().value("name").toString())
                        .rel(edge.toString())
                        .object(edge.outVertex().value("name").toString())
                        .build();
            }
        } finally {
            client.close();
            cluster.close();
        }

        return fact;
    }

    public List<String> find(Fact query) {
        return null;
    }

    public long count(Fact query) {
        long count = 0;

        Cluster cluster = Cluster.open();
        Client client = cluster.connect();

        try {
            Map<String,Object> params = new HashMap<>();
            params.put("?", query.getSubject());
            ResultSet results = client.submit("g.V().has('name','?').in('isa').out('?').where(.....).count()", params);
            Result result = results.one();
            if (result != null) {
                count = result.getLong();
            }
        } finally {
            client.close();
            cluster.close();
        }

        return count;
    }
}
