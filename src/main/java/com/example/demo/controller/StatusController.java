package com.example.demo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StatusController {
    @GetMapping(value = "/ping")
    @ResponseStatus(value = HttpStatus.OK)
    public String isRunning() {
        return "I'm Alive!";
    }
}
