package com.bdang.controller;

import com.bdang.controller.exception.FactQueryParseException;
import com.bdang.controller.exception.InvalidFactQueryException;
import com.bdang.facts.Fact;
import com.bdang.storage.Accessor;
import com.bdang.storage.exception.UnregisteredConceptException;
import com.google.gson.Gson;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class FactQueryControllerTest {

    @Test
    public void testFind() throws Exception {
        Fact query = new Fact.Builder().subject("animal").rel("has").object("legs").build();
        List<String> animals = Arrays.asList("otter", "fox", "moose");

        Accessor accessor = mock(Accessor.class);
        when(accessor.find(eq(query))).thenReturn(animals);

        FactQueryController controller = new FactQueryController(accessor);
        String response = controller.find("animal", "has", "legs");

        List<String> output = Arrays.asList(new Gson().fromJson(response, String[].class));
        assertThat(output, hasItems("otter", "fox", "moose"));
    }

    @Test
    public void testFindParseError() throws Exception {
        FactQueryController controller = new FactQueryController(mock(Accessor.class));
        try {
            controller.find(null, null, null);
            fail("Expected an exception to be thrown, but didn't catch one.");
        } catch (Exception e) {
            assertTrue(e instanceof FactQueryParseException);
        }
    }

    @Test
    public void testFindInvalidRelationship() throws Exception {
        FactQueryController controller = new FactQueryController(mock(Accessor.class));
        try {
            controller.find("otter", "contemplates", "existence");
            fail("Expected an exception to be thrown, but didn't catch one.");
        } catch (Exception e) {
            assertTrue(e instanceof FactQueryParseException);
        }
    }

    @Test
    public void testFindInvalidQuery() throws Exception {
        Fact query = new Fact.Builder().subject("animal").rel("has").object("toenails").build();

        Accessor accessor = mock(Accessor.class);
        when(accessor.find(eq(query))).thenThrow(new UnregisteredConceptException("toenails"));

        FactQueryController controller = new FactQueryController(accessor);

        try {
            controller.find("animal", "has", "toenails");
            fail("Expected an exception to be thrown, but didn't catch one.");
        } catch(Exception e) {
            assertTrue(e instanceof InvalidFactQueryException);
        }
    }

    @Test
    public void testCount() throws Exception {
        Fact query = new Fact.Builder().subject("animal").rel("has").object("legs").build();

        Accessor accessor = mock(Accessor.class);
        when(accessor.count(eq(query))).thenReturn(3L);

        FactQueryController controller = new FactQueryController(accessor);
        String response = controller.count("animal", "has", "legs");

        assertThat(response, equalTo("3"));
    }

    @Test
    public void testCountParseError() throws Exception {
        FactQueryController controller = new FactQueryController(mock(Accessor.class));
        try {
            controller.count(null, null, null);
            fail("Expected an exception to be thrown, but didn't catch one.");
        } catch (Exception e) {
            assertTrue(e instanceof FactQueryParseException);
        }
    }

    @Test
    public void testCountInvalidRelationship() throws Exception {
        FactQueryController controller = new FactQueryController(mock(Accessor.class));
        try {
            controller.count("otter", "contemplates", "existence");
            fail("Expected an exception to be thrown, but didn't catch one.");
        } catch (Exception e) {
            assertTrue(e instanceof FactQueryParseException);
        }
    }

    @Test
    public void testCountInvalidQuery() throws Exception {
        Fact query = new Fact.Builder().subject("animal").rel("has").object("toenails").build();

        Accessor accessor = mock(Accessor.class);
        when(accessor.count(eq(query))).thenThrow(new UnregisteredConceptException("toenails"));

        FactQueryController controller = new FactQueryController(accessor);

        try {
            controller.count("animal", "has", "toenails");
            fail("Expected an exception to be thrown, but didn't catch one.");
        } catch(Exception e) {
            assertTrue(e instanceof InvalidFactQueryException);
        }
    }
}