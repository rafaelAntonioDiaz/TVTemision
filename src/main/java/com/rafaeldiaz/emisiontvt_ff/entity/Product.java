package com.rafaeldiaz.emisiontvt_ff.entity;

import java.util.List;

public record Product(
        String name,
        String domain,
        String domainEn,
        String domainLabelEs,
        String domainLabelEn,
        String slugEs,
        String slugEn,
        String patent,
        String imageFolder,      // null si no hay imágenes
        String heroImage,        // "fig01.webp" o null
        List<String> gallery,    // lista de filenames, puede ser vac
        Content es,
        Content en
) {
    public record Content(
            String tagline,
            String intro,
            String problem,
            List<String> inputs,
            List<String> outputs,
            List<String> benefits,
            List<String> evidence,
            List<String> tech
    ) {}
    public boolean hasHero()   { return heroImage != null && !heroImage.isBlank(); }
    public boolean hasGallery(){ return gallery != null && !gallery.isEmpty(); }
    public boolean hasImages() { return imageFolder != null && !imageFolder.isBlank(); }
}