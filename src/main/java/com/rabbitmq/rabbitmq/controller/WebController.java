package com.rabbitmq.rabbitmq.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller để phục vụ giao diện web demo
 */
@Controller
public class WebController {

    /**
     * Trang chủ - Demo giao diện
     */
    @GetMapping("/")
    public String index() {
        return "forward:/index.html";
    }
}
