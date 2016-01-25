package com.bdang.controller;

import com.bdang.controller.exception.FactQueryParseException;
import com.bdang.controller.exception.InvalidFactQueryException;
import com.bdang.facts.Fact;
import com.bdang.storage.Accessor;
import com.bdang.storage.AccessorFactory;
import com.bdang.storage.DBLocation;
import com.bdang.storage.exception.UnregisteredConceptException;
import com.google.common.annotations.VisibleForTesting;
import com.google.gson.Gson;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class FactQueryController {
    private final Accessor accessor;

    public FactQueryController() {
        accessor = AccessorFactory.getAccessor(DBLocation.INMEMORY);
    }

    @VisibleForTesting
    FactQueryController(Accessor accessor) {
        this.accessor = accessor;
    }

    @RequestMapping(value = "/animals/which", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public String find(@RequestParam(value = "s", required = false) String subject,
                       @RequestParam(value = "r", required = false) String rel,
                       @RequestParam(value = "o", required = false) String object) {
        Fact query;
        try {
            query = new Fact.Builder().subject(subject).rel(rel).object(object).build();
        } catch (NullPointerException|IllegalArgumentException e) {
            throw new FactQueryParseException(e, subject, rel, object);
        }

        try {
            List<String> results = accessor.find(query);
            return new Gson().toJson(results);
        } catch (UnregisteredConceptException e) {
            throw new InvalidFactQueryException(query, e);
        }
    }

    @RequestMapping(value = "/animals/how-many", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public String count(@RequestParam(value = "s", required = false) String subject,
                        @RequestParam(value = "r", required = false) String rel,
                        @RequestParam(value = "o", required = false) String object) {
        Fact query;
        try {
            query = new Fact.Builder().subject(subject).rel(rel).object(object).build();
        } catch (NullPointerException|IllegalArgumentException e) {
            throw new FactQueryParseException(e, subject, rel, object);
        }

        try {
            long count = accessor.count(query);
            return Long.toString(count);
        } catch (UnregisteredConceptException e) {
            throw new InvalidFactQueryException(query, e);
        }
    }
}
