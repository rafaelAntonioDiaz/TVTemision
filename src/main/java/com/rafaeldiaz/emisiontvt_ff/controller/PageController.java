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
    private final ProductService products;

    public PageController(ProductService products) {
        this.products = products;
    }

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

    // ---------- ES ----------
    @GetMapping("/soluciones")
    public String soluciones(Model model) {
        model.addAttribute("productsByDomain", products.groupedByDomainEs());
        model.addAttribute("lang", "es");
        return "soluciones";
    }

    @GetMapping("/soluciones/{categoria}/{producto}")
    public String solucionDetalle(@PathVariable String categoria,
                                  @PathVariable String producto,
                                  Model model) {
        Product p = products.findByEs(categoria, producto)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("product", p);
        model.addAttribute("content", p.es());
        model.addAttribute("domainLabel", p.domainLabelEs());
        model.addAttribute("lang", "es");
        return "soluciones-producto";
    }

    // ---------- EN ----------
    @GetMapping("/en/solutions")
    public String solutionsEn(Model model) {
        model.addAttribute("productsByDomain", products.groupedByDomainEn());
        model.addAttribute("lang", "en");
        return "en-solutions";
    }

    @GetMapping("/en/solutions/{categoria}/{producto}")
    public String solutionDetailEn(@PathVariable String categoria,
                                   @PathVariable String producto,
                                   Model model) {
        Product p = products.findByEn(categoria, producto)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("product", p);
        model.addAttribute("content", p.en());
        model.addAttribute("domainLabel", p.domainLabelEn());
        model.addAttribute("lang", "en");
        return "en-solutions-product";
    }
}