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
}