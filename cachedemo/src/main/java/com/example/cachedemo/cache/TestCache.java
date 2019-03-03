package com.example.cachedemo.cache;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class TestCache {
    @Cacheable(value="myname", key="#name")
    public String getName(String name){
        return "hello:"+name;
    }
}
