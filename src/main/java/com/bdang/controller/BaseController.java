package com.bdang.controller;

import org.apache.tinkerpop.gremlin.driver.Client;
import org.apache.tinkerpop.gremlin.driver.Cluster;
import org.apache.tinkerpop.gremlin.driver.Result;
import org.apache.tinkerpop.gremlin.driver.ResultSet;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.function.Consumer;

@Controller
public class BaseController {

    private static final String VIEW_COUNT = "count";
    private static final String VIEW_FACT = "fact";
    private static final String VIEW_FACTID = "factid";
    private static final String VIEW_FIND = "find";

    @RequestMapping(value = "/animals/facts", method = RequestMethod.POST, produces = "application/json")
    public String putFact(ModelMap model) {
        String id = "0b3431e3-2351-46f1-ad90-fa022a60ba15";
        model.addAttribute("id", id);
        return VIEW_FACTID;
    }

    @RequestMapping(value = "/animals/facts/{id}", method = RequestMethod.GET, produces = "application/json")
    public String getFact(ModelMap model, @PathVariable("id") String id) {
        String subject = "otter";
        String rel = "rel";
        String object = "object";
        model.addAttribute("subject", subject);
        model.addAttribute("rel", rel);
        model.addAttribute("object", object);
        return VIEW_FACT;
    }


    @RequestMapping(value = "/animals/facts/{id}", method = RequestMethod.DELETE, produces = "application/json")
    public String deleteFact(ModelMap model, @PathVariable("id") String id) {
        model.addAttribute("id", id);
        return VIEW_FACTID;
    }

    @RequestMapping(value = "/animals/which", method = RequestMethod.GET, produces = "application/json")
    public String find(ModelMap model) {
        String[] objects = { "otter", "fox", "moose" };
        model.addAttribute("objects", objects);
        return VIEW_FIND;
    }

    @RequestMapping(value = "/animals/how-many", method = RequestMethod.GET, produces = "application/json")
    public String count(ModelMap model) {
        long count = 3;
        model.addAttribute("count", count);
        return VIEW_COUNT;
    }
}
