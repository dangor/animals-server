package com.bdang.controller;

import com.bdang.controller.exception.FactIdNotFoundException;
import com.bdang.controller.exception.FactParseException;
import com.bdang.facts.Fact;
import com.bdang.storage.Accessor;
import com.bdang.storage.AccessorFactory;
import com.google.common.annotations.VisibleForTesting;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class FactManagementController {
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
        Fact fact;
        try {
            fact = new Gson().fromJson(body, Fact.Builder.class).build();
        } catch (JsonSyntaxException|NullPointerException|IllegalArgumentException e) {
            throw new FactParseException(body, e);
        }

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
    @ResponseBody
    public String getFact(@PathVariable("id") String id) {
        Fact fact = accessor.get(id);
        if (fact == null) {
            throw new FactIdNotFoundException(id);
        }

        return new Gson().toJson(fact);
    }

    @RequestMapping(value = "/animals/facts/{id}", method = RequestMethod.DELETE, produces = "application/json")
    @ResponseBody
    public String deleteFact(@PathVariable("id") String id) {
        boolean deleted = accessor.delete(id);
        if (!deleted) {
            throw new FactIdNotFoundException(id);
        }

        IdResponse response = new IdResponse(id);
        return new Gson().toJson(response);
    }

    @RequestMapping(value = "/animals/facts/all", method = RequestMethod.DELETE)
    @ResponseBody
    public String deleteAllFacts() {
        boolean deleted = accessor.deleteAll();
        return deleted ? "Clean" : "Failed";
    }

    @ExceptionHandler(FactParseException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ResponseBody
    public String factParseError() {
        MessageResponse output = new MessageResponse("Failed to parse your fact");
        return new Gson().toJson(output);
    }

    private final class MessageResponse {
        private String message;
        private MessageResponse(String message) {
            this.message = message;
        }
    }

    @ExceptionHandler(FactIdNotFoundException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    @ResponseBody
    public String factIdNotFound() {
        return "";
    }
}
