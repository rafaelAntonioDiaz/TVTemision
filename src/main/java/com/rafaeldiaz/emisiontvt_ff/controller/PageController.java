package com.rafaeldiaz.emisiontvt_ff.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/")
    public String root() {
        return "redirect:/brochure";
    }

    @GetMapping("/home")
    public String home() {
        return "home";
    }

    @GetMapping("/amigosyfamilia")
    public String amigosYFamilia() {
        return "amigosyfamilia";
    }

    @GetMapping("/primera_emision")
    public String primeraEmision() {
        return "primera_emision";
    }

    @GetMapping("/segunda_emision")
    public String segundaEmision() {
        return "segunda_emision";
    }
    
    @GetMapping("/tecnologia")
    public String tecnologia() {
        return "tecnologia";
    }

    @GetMapping("/brochure")
    public String brochure() {
        return "brochure";
    }
    @GetMapping("/brochure-en")
    public String brochureEn() {
        return "brochure-en";
    }
}