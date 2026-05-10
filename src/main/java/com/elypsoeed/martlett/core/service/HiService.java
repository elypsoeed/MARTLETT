package com.elypsoeed.martlett.core.service;

import org.springframework.stereotype.Service;

@Service
public class HiService {

    public String sayHi(String greeting) {
        return "Hi, %s".formatted(greeting);
    }
}
