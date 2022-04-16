package com.example.imgr.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IndexController {
    @GetMapping("/ping")
    public String ping() {
        return "pong!";
    }
}
