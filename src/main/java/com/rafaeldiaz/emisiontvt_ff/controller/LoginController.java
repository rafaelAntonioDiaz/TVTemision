package com.rafaeldiaz.emisiontvt_ff.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @GetMapping("/admin/login")
    public String adminLogin() {
        return "admin/login";   // busca admin/login.html en templates
    }
}