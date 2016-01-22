package com.bdang.controller;

import com.bdang.facts.Fact;
import com.bdang.storage.AccessorFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class BaseController {

    private static final String VIEW_BLANK = "blank";
    private static final String VIEW_COUNT = "count";
    private static final String VIEW_FACT = "fact";
    private static final String VIEW_FACTID = "factid";
    private static final String VIEW_FIND = "find";
    private static final String VIEW_PARSEERROR = "parserror";

    @RequestMapping(value = "/animals/facts", method = RequestMethod.POST, produces = "application/json")
    public String putFact(ModelMap model,
                          @RequestParam(value = "subject", required = false) String subject,
                          @RequestParam(value = "rel", required = false) String rel,
                          @RequestParam(value = "object", required = false) String object) {
        Fact fact;
        try {
            fact = new Fact.Builder().subject(subject).rel(rel).object(object).build();
        } catch (NullPointerException|IllegalArgumentException e) {
            return VIEW_PARSEERROR;
        }

        String id = AccessorFactory.getAccessor().put(fact);
        model.addAttribute("id", id);
        return VIEW_FACTID;
    }

    @RequestMapping(value = "/animals/facts/{id}", method = RequestMethod.GET, produces = "application/json")
    public String getFact(ModelMap model, @PathVariable("id") String id) {
        Fact fact = AccessorFactory.getAccessor().get(id);
        if (fact == null) {
            return VIEW_BLANK;
        }
        model.addAttribute("subject", fact.getSubject());
        model.addAttribute("rel", fact.getRel().toString());
        model.addAttribute("object", fact.getObject());
        return VIEW_FACT;
    }

    @RequestMapping(value = "/animals/facts/{id}", method = RequestMethod.DELETE, produces = "application/json")
    public String deleteFact(ModelMap model, @PathVariable("id") String id) {
        boolean deleted = AccessorFactory.getAccessor().delete(id);
        if (!deleted) {
            return VIEW_BLANK;
        }

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
