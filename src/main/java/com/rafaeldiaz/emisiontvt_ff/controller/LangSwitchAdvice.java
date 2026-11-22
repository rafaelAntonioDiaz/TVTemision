package com.rafaeldiaz.emisiontvt_ff.controller;

import com.rafaeldiaz.emisiontvt_ff.entity.Product;
import com.rafaeldiaz.emisiontvt_ff.entity.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class LangSwitchAdvice {

    private final ProductService products;

    public LangSwitchAdvice(ProductService products) {
        this.products = products;
    }

    @ModelAttribute("langSwitchUrl")
    public String langSwitchUrl(HttpServletRequest req) {
        String uri = req.getRequestURI();
        String ctx = req.getContextPath();
        if (ctx != null && !ctx.isEmpty() && uri.startsWith(ctx)) uri = uri.substring(ctx.length());

        // --- Soluciones (con lookup de productos por dominio/slug) ---
        if (uri.equals("/soluciones")) return "/en/solutions";
        if (uri.equals("/en/solutions")) return "/soluciones";

        if (uri.startsWith("/soluciones/")) {
            String[] p = uri.split("/");
            if (p.length >= 4) {
                Product prod = products.findByEs(p[2], p[3]).orElse(null);
                if (prod != null) return "/en/solutions/" + prod.domainEn() + "/" + prod.slugEn();
            }
        }
        if (uri.startsWith("/en/solutions/")) {
            String[] p = uri.split("/");
            if (p.length >= 5) {
                Product prod = products.findByEn(p[3], p[4]).orElse(null);
                if (prod != null) return "/soluciones/" + prod.domain() + "/" + prod.slugEs();
            }
        }

        // --- Pares explícitos ES <-> EN ---
        switch (uri) {
            case "/home":           return "/en/home";
            case "/en/home":        return "/home";
            case "/tecnologia":     return "/en/technology";
            case "/en/technology":  return "/tecnologia";
            case "/contacto":       return "/en/contact";
            case "/en/contact":     return "/contacto";
            case "/brochure":       return "/brochure-en";
            case "/brochure-en":    return "/brochure";
            case "/entrenamiento":  return "/en/training";
            case "/en/training":    return "/entrenamiento";
            case "/recursos":       return "/en/resources";
            case "/en/resources":   return "/recursos";
            case "/nosotros":       return "/en/about";
            case "/en/about":       return "/nosotros";

        }

        // --- Fallback: páginas sin versión EN (inversionistas, amigosyfamilia,
        //     primera_emision, segunda_emision) llevan a la home del otro idioma ---
        if (uri.startsWith("/en/")) return "/home";
        return "/en/home";
    }
}