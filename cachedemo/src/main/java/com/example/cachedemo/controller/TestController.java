package com.example.cachedemo.controller;

import com.example.cachedemo.cache.TestCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    @Autowired
    private TestCache testCache;
    @GetMapping("/getCache/{name}")
    public String getCache(@PathVariable String name) {
        return testCache.getName(name);
    }
}
