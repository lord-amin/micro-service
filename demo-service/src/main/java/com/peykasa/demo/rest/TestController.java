package com.peykasa.demo.rest;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TestController {
    private final static Logger LOGGER = LoggerFactory.getLogger(TestController.class);
    @Value("${app.prop}")
    private String prop;

    @GetMapping("/hello")
    public String refresh() {
        return "hello " + prop;

    }


}
