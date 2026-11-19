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
public class ProductController {

    private final ProductService products;

    public ProductController(ProductService products) {
        this.products = products;
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
        model.addAttribute("detail", p.es());   // antes: "content"
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
        model.addAttribute("detail", p.en());   // antes: "content"
        model.addAttribute("domainLabel", p.domainLabelEn());
        model.addAttribute("lang", "en");
        return "en-solutions-product";
    }
}