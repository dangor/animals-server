package com.bdang.controller;

import com.bdang.facts.Fact;
import com.bdang.storage.Accessor;
import com.bdang.storage.AccessorFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class BaseController {

    private static final String VIEW_COUNT = "count";
    private static final String VIEW_FACT = "fact";
    private static final String VIEW_FACTID = "factid";
    private static final String VIEW_FIND = "find";

    @RequestMapping(value = "/animals/facts", method = RequestMethod.POST, produces = "application/json")
    public String putFact(ModelMap model,
                          @RequestParam(value = "subject", required = true) String subject,
                          @RequestParam(value = "rel", required = true) String rel,
                          @RequestParam(value = "object", required = true) String object) {
        Fact fact = new Fact.Builder().subject(subject).rel(rel).object(object).build();
        String id = AccessorFactory.getAccessor().put(fact);
        model.addAttribute("id", id);
        return VIEW_FACTID;
    }

    @RequestMapping(value = "/animals/facts/{id}", method = RequestMethod.GET, produces = "application/json")
    public String getFact(ModelMap model, @PathVariable("id") String id) {
        Fact fact = AccessorFactory.getAccessor().get(id);
        model.addAttribute("subject", fact.getSubject());
        model.addAttribute("rel", fact.getRel());
        model.addAttribute("object", fact.getObject());
        return VIEW_FACT;
    }


    @RequestMapping(value = "/animals/facts/{id}", method = RequestMethod.DELETE, produces = "application/json")
    public String deleteFact(ModelMap model, @PathVariable("id") String id) {
        AccessorFactory.getAccessor().delete(id);
        model.addAttribute("id", id);
        return VIEW_FACTID;
    }

    @RequestMapping(value = "/animals/which", method = RequestMethod.GET, produces = "application/json")
    public String find(ModelMap model,
                       @RequestParam(value = "s", required = true) String subject,
                       @RequestParam(value = "r", required = true) String rel,
                       @RequestParam(value = "o", required = true) String object) {
        Fact query = new Fact.Builder().subject(subject).rel(rel).object(object).build();
        List<String> results = AccessorFactory.getAccessor().find(query);
        model.addAttribute("results", results);
        return VIEW_FIND;
    }

    @RequestMapping(value = "/animals/how-many", method = RequestMethod.GET, produces = "application/json")
    public String count(ModelMap model,
                        @RequestParam(value = "s", required = true) String subject,
                        @RequestParam(value = "r", required = true) String rel,
                        @RequestParam(value = "o", required = true) String object) {
        Fact query = new Fact.Builder().subject(subject).rel(rel).object(object).build();
        long count = AccessorFactory.getAccessor().count(query);
        model.addAttribute("count", count);
        return VIEW_COUNT;
    }
}
