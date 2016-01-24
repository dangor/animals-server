package com.bdang.controller;

import com.bdang.facts.Fact;
import com.bdang.storage.Accessor;
import com.bdang.storage.AccessorFactory;
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
        accessor = AccessorFactory.getAccessor();
    }

    @VisibleForTesting
    FactQueryController(Accessor accessor) {
        this.accessor = accessor;
    }

    @RequestMapping(value = "/animals/which", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public String find(@RequestParam(value = "s", required = true) String subject,
                       @RequestParam(value = "r", required = true) String rel,
                       @RequestParam(value = "o", required = true) String object) {
        Fact query = new Fact.Builder().subject(subject).rel(rel).object(object).build();

        List<String> results = accessor.find(query);

        return new Gson().toJson(results);
    }

    @RequestMapping(value = "/animals/how-many", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public String count(@RequestParam(value = "s", required = true) String subject,
                        @RequestParam(value = "r", required = true) String rel,
                        @RequestParam(value = "o", required = true) String object) {
        Fact query = new Fact.Builder().subject(subject).rel(rel).object(object).build();

        long count = accessor.count(query);

        return Long.toString(count);
    }
}
