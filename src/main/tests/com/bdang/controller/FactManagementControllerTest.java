package com.bdang.controller;

import com.bdang.controller.exception.FactIdNotFoundException;
import com.bdang.controller.exception.FactParseException;
import com.bdang.facts.Fact;
import com.bdang.storage.Accessor;
import com.google.gson.Gson;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class FactManagementControllerTest {

    @Test
    public void testPutFact() throws Exception {
        Fact fact = new Fact.Builder().subject("otter").rel("lives").object("river").build();
        String id = "fact-id";

        Accessor accessor = mock(Accessor.class);
        when(accessor.put(eq(fact))).thenReturn(id);

        FactManagementController controller = new FactManagementController(accessor);

        String response = controller.putFact(new Gson().toJson(fact));

        IdResponse output = new Gson().fromJson(response, IdResponse.class);
        assertThat(output.id, equalTo(id));
    }

    private static final class IdResponse {
        private String id;
    }

    @Test
    public void testPutFactParseError() throws Exception {
        FactManagementController controller = new FactManagementController(mock(Accessor.class));
        try {
            controller.putFact("");
            fail("Expected an exception to be thrown, but didn't catch one.");
        } catch (Exception e) {
            assertThat(e, instanceOf(FactParseException.class));
        }
    }

    @Test
    public void testPutFactInvalidRelationship() throws Exception {
        FactManagementController controller = new FactManagementController(mock(Accessor.class));
        try {
            controller.putFact("{ \"subject\": \"otter\", \"rel\": \"contemplates\", \"object\": \"existence\" }");
            fail("Expected an exception to be thrown, but didn't catch one.");
        } catch (Exception e) {
            assertThat(e, instanceOf(FactParseException.class));
        }
    }

    @Test
    public void testGetFact() throws Exception {
        Fact fact = new Fact.Builder().subject("otter").rel("lives").object("river").build();
        String id = "fact-id";

        Accessor accessor = mock(Accessor.class);
        when(accessor.get(eq(id))).thenReturn(fact);

        FactManagementController controller = new FactManagementController(accessor);
        String response = controller.getFact(id);

        Fact output = new Gson().fromJson(response, Fact.Builder.class).build();
        assertThat(output, equalTo(fact));
    }

    @Test
    public void testGetFactInvalidId() throws Exception {
        String id = "invalid-id";

        Accessor accessor = mock(Accessor.class);
        when(accessor.get(eq(id))).thenReturn(null);

        FactManagementController controller = new FactManagementController(accessor);
        try {
            controller.getFact(id);
            fail("Expected an exception to be thrown, but didn't catch one.");
        } catch (Exception e) {
            assertThat(e, instanceOf(FactIdNotFoundException.class));
        }
    }

    @Test
    public void testDeleteFact() throws Exception {
        String id = "fact-id";

        Accessor accessor = mock(Accessor.class);
        when(accessor.delete(eq(id))).thenReturn(true);

        FactManagementController controller = new FactManagementController(accessor);
        String response = controller.deleteFact(id);

        IdResponse output = new Gson().fromJson(response, IdResponse.class);
        assertThat(output.id, equalTo(id));
    }

    @Test
    public void testDeleteFactInvalidId() throws Exception {
        String id = "invalid-id";

        Accessor accessor = mock(Accessor.class);
        when(accessor.delete(eq(id))).thenReturn(false);

        FactManagementController controller = new FactManagementController(accessor);
        try {
            controller.deleteFact(id);
            fail("Expected an exception to be thrown, but didn't catch one.");
        } catch (Exception e) {
            assertThat(e, instanceOf(FactIdNotFoundException.class));
        }
    }
}