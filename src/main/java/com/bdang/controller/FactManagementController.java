package com.bdang.controller;

import com.bdang.facts.Fact;
import com.bdang.storage.Accessor;
import com.bdang.storage.AccessorFactory;
import com.google.common.annotations.VisibleForTesting;
import com.google.gson.Gson;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class FactManagementController {
    private static final String SUBJECT = "subject";
    private static final String REL = "rel";
    private static final String OBJECT = "object";
    private static final String ID = "id";

    private final Accessor accessor;

    public FactManagementController() {
        accessor = AccessorFactory.getAccessor();
    }

    @VisibleForTesting
    FactManagementController(Accessor accessor) {
        this.accessor = accessor;
    }

    @RequestMapping(value = "/animals/facts", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public String putFact(@RequestBody String body) {
        Fact fact = new Gson().fromJson(body, Fact.Builder.class).build();

        String id = accessor.put(fact);

        IdResponse response = new IdResponse(id);
        return new Gson().toJson(response);
    }

    private final class IdResponse {
        private String id;
        private IdResponse(String id) {
            this.id = id;
        }
    }

    @RequestMapping(value = "/animals/facts/{id}", method = RequestMethod.GET, produces = "application/json")
    public String getFact(@PathVariable("id") String id) {
        Fact fact = accessor.get(id);
        if (fact == null) {
            return "";
        }

        return new Gson().toJson(fact);
    }

    @RequestMapping(value = "/animals/facts/{id}", method = RequestMethod.DELETE, produces = "application/json")
    public String deleteFact(@PathVariable("id") String id) {
        boolean deleted = accessor.delete(id);
        if (!deleted) {
            return "";
        }

        IdResponse response = new IdResponse(id);
        return new Gson().toJson(response);
    }
}
