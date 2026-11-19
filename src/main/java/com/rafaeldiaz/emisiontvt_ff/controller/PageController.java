package com.rafaeldiaz.emisiontvt_ff.controller;

import com.rafaeldiaz.emisiontvt_ff.entity.Product;
import com.rafaeldiaz.emisiontvt_ff.entity.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class PageController {

    @GetMapping("/")
    public String root() { return "redirect:/home"; }

    @GetMapping("/home")
    public String home() { return "home"; }

    @GetMapping("/en/home")
    public String homeEn() { return "en-home"; }


    @GetMapping("/inversionistas")
    public String inversionistas() {
        return "inversionistas";
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