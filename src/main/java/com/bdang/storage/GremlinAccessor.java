package com.bdang.storage;

import com.bdang.facts.Fact;
import org.apache.tinkerpop.gremlin.driver.Client;
import org.apache.tinkerpop.gremlin.driver.Cluster;
import org.apache.tinkerpop.gremlin.driver.Result;
import org.apache.tinkerpop.gremlin.driver.ResultSet;
import org.apache.tinkerpop.gremlin.structure.Edge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

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
            params.put("x", fact.getSubject());
            params.put("y", fact.getRel().toString());
            params.put("z", fact.getObject());
            Result result = client.submit("g.V().has('name','x')", params).one();
            if (result == null) {
                client.submit("g.addV('name','x')", params);
            }

            result = client.submit("g.V().has('name','z')", params).one();
            if (result == null) {
                client.submit("g.addV('name','z')", params);
            }

            result = client.submit("g.E().hasLabel('y').and(outV().has('name','x'),inV().has('name','z'))", params).one();
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
        final List<String> findResults = new ArrayList<>();

        Cluster cluster = Cluster.open();
        Client client = cluster.connect();

        try {
            Map<String,Object> params = new HashMap<>();
            params.put("x", query.getSubject());
            params.put("y", query.getRel().toString());
            params.put("z", query.getObject());
            ResultSet results = client.submit("g.V().has('name','x').inV('isa').as('a').outV('y').has('name','z').inV('y').where(within('a'))", params);
            results.forEach(new Consumer<Result>() {
                @Override
                public void accept(Result result) {
                    findResults.add(result.getVertex().value("name").toString());
                }
            });
        } finally {
            client.close();
            cluster.close();
        }

        return findResults;
    }

    public long count(Fact query) {
        long count = 0;

        Cluster cluster = Cluster.open();
        Client client = cluster.connect();

        try {
            Map<String,Object> params = new HashMap<>();
            params.put("x", query.getSubject());
            params.put("y", query.getRel().toString());
            params.put("z", query.getObject());
            ResultSet results = client.submit("g.V().has('name','x').inV('isa').outV('y').has('name','z').count()", params);
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
