package com.bdang.controller;

import com.bdang.facts.Fact;
import com.bdang.storage.Accessor;
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
    public void testCount() throws Exception {
        Fact query = new Fact.Builder().subject("animal").rel("has").object("legs").build();

        Accessor accessor = mock(Accessor.class);
        when(accessor.count(eq(query))).thenReturn(3L);

        FactQueryController controller = new FactQueryController(accessor);
        String response = controller.count("animal", "has", "legs");

        assertThat(response, equalTo("3"));
    }
}