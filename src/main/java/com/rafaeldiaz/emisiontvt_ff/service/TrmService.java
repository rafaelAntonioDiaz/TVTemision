package com.rafaeldiaz.emisiontvt_ff.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;

@Service
public class TrmService {

    @Autowired
    private RestTemplate restTemplate;

    private static final String API_URL = "https://api.exchangerate-api.com/v4/latest/USD";

    /**
     * Obtiene la TRM (tasa de cambio USD a COP) desde la API pública.
     * @return BigDecimal con el valor de 1 USD en COP
     * @throws RuntimeException si la API falla
     */
    public BigDecimal getTrm() {
        try {
            Map<String, Object> response = restTemplate.getForObject(API_URL, Map.class);
            if (response != null && response.containsKey("rates")) {
                Map<String, Double> rates = (Map<String, Double>) response.get("rates");
                Double copRate = rates.get("COP");
                if (copRate != null) {
                    return BigDecimal.valueOf(copRate);
                }
            }
        } catch (Exception e) {
            // Podrías loguear el error y usar un valor de respaldo si lo deseas
            throw new RuntimeException("No se pudo obtener la TRM. Intenta más tarde.", e);
        }
        throw new RuntimeException("TRM no disponible en este momento.");
    }
}