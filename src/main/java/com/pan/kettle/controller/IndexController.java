package com.pan.kettle.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;
import java.net.UnknownHostException;

@RestController
public class IndexController {
    @GetMapping("/")
    private String init(){
        try {
            return "welcome ！time:"+System.currentTimeMillis()+",ip:"+ InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return "fail";
        }
    }
}
