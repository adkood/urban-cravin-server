package com.ashutosh.urban_cravin.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/public/hello")
    public String hello() {
        return "Hello from server :)";
    }

}
