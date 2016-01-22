package com.bdang.controller;

import org.apache.tinkerpop.gremlin.driver.Client;
import org.apache.tinkerpop.gremlin.driver.Cluster;
import org.apache.tinkerpop.gremlin.driver.Result;
import org.apache.tinkerpop.gremlin.driver.ResultSet;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.function.Consumer;

@Controller
public class BaseController {

    private static final String VIEW_INDEX = "index";
    private static final String VIEW_JSON = "json";
    private final static org.slf4j.Logger logger = LoggerFactory.getLogger(BaseController.class);

    @RequestMapping(value = "/comics", method = RequestMethod.GET)
    public String comics(ModelMap model) {

        Cluster cluster = Cluster.open();
        Client client = cluster.connect();

        final StringBuilder builder = new StringBuilder();

        ResultSet resultSet = client.submit("g.V().has('comic-book', 'AVF 4').in('appeared').has('weapon', without('shield','claws')).values('character').order()");
        resultSet.stream().forEach(new Consumer<Result>() {
            public void accept(Result result) {
                builder.append(result.getString());
                builder.append("\n");
            }
        });

        model.addAttribute("message", "Comics: " + builder.toString());

        // Spring uses InternalResourceViewResolver and return back index.jsp
        return VIEW_INDEX;
    }

    @RequestMapping(value = "/animals/facts", method = RequestMethod.POST)
    public String putFact(ModelMap model) {
        String factId = "0b3431e3-2351-46f1-ad90-fa022a60ba15";
        model.addAttribute("jsonOutput", "{ \"id\": \"" + factId + "\" }");
        return VIEW_INDEX;
    }
}
