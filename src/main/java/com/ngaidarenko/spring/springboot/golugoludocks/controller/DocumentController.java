package com.ngaidarenko.spring.springboot.golugoludocks.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class DocumentController {

    @MessageMapping("/edit")
    @SendTo("/topic/changes")
    public String processChanges(String message) {
        return message;
    }
}
