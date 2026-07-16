package com.rafaeldiaz.emisiontvt_ff;

import com.rafaeldiaz.emisiontvt_ff.entity.Round;
import com.rafaeldiaz.emisiontvt_ff.repository.RoundRepository;
import com.rafaeldiaz.emisiontvt_ff.config.FractalParams;
import com.rafaeldiaz.emisiontvt_ff.service.FractalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.awt.image.BufferedImage;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class FractalUniquenessTest {

    @Autowired
    private FractalService fractalService;

    @Autowired
    private RoundRepository roundRepo;

    private Round rondaFF;
    private Round rondaHito1;

    @BeforeEach
    void setUp() {
        rondaFF = roundRepo.findAll().stream()
                .filter(r -> r.getName().equals("F&F"))
                .findFirst().orElseThrow();
        rondaHito1 = roundRepo.findAll().stream()
                .filter(r -> r.getName().equals("HITO1"))
                .findFirst().orElseThrow();
    }

    /**
     * Dos tokens del mismo inversor, misma ronda → fractales distintos (píxeles diferentes).
     */
    @Test
    void fractalesMismoInversorMismaRondaDebenSerDiferentes() {
        Long investorId = 1L;
        FractalParams params1 = fractalService.generateParams(investorId, "TVT-F0001", "F&F");
        FractalParams params2 = fractalService.generateParams(investorId, "TVT-F0002", "F&F");

        BufferedImage img1 = fractalService.renderStatic(params1, 200, 150);
        BufferedImage img2 = fractalService.renderStatic(params2, 200, 150);

        assertFalse(imagesAreEqual(img1, img2),
                "Los fractales de un mismo inversor deben ser distintos entre sí");
    }

    /**
     * Tokens de distinto inversor (misma ronda) → fractales diferentes.
     */
    @Test
    void fractalesDistintoInversorMismaRondaDebenSerDiferentes() {
        FractalParams params1 = fractalService.generateParams(1L, "TVT-F0001", "F&F");
        FractalParams params2 = fractalService.generateParams(2L, "TVT-F0002", "F&F");

        BufferedImage img1 = fractalService.renderStatic(params1, 200, 150);
        BufferedImage img2 = fractalService.renderStatic(params2, 200, 150);

        assertFalse(imagesAreEqual(img1, img2),
                "Fractales de distintos inversores deben ser distintos");
    }

    /**
     * Misma ronda, mismo inversor, mismo código → ¡debe ser idéntico! (determinista).
     */
    @Test
    void mismoTokenDebeGenerarMismoFractal() {
        FractalParams params1 = fractalService.generateParams(1L, "TVT-F0001", "F&F");
        FractalParams params2 = fractalService.generateParams(1L, "TVT-F0001", "F&F");

        BufferedImage img1 = fractalService.renderStatic(params1, 200, 150);
        BufferedImage img2 = fractalService.renderStatic(params2, 200, 150);

        assertTrue(imagesAreEqual(img1, img2),
                "El mismo token debe producir exactamente el mismo fractal");
    }

    /**
     * Tokens de rondas diferentes → paletas de colores distintas.
     */
    @Test
    void rondasDiferentesTienenPaletasDiferentes() {
        FractalParams paramsFF = fractalService.generateParams(1L, "TVT-F0001", "F&F");
        FractalParams paramsHito1 = fractalService.generateParams(1L, "TVT-F0001", "HITO1");

        List<String> paletteFF = paramsFF.getPalette();
        List<String> paletteHito1 = paramsHito1.getPalette();

        assertNotEquals(paletteFF, paletteHito1,
                "Las paletas de colores deben ser diferentes entre rondas");
    }

    /**
     * Compara dos BufferedImage píxel a píxel.
     */
    private boolean imagesAreEqual(BufferedImage img1, BufferedImage img2) {
        if (img1.getWidth() != img2.getWidth() || img1.getHeight() != img2.getHeight()) {
            return false;
        }
        for (int x = 0; x < img1.getWidth(); x++) {
            for (int y = 0; y < img1.getHeight(); y++) {
                if (img1.getRGB(x, y) != img2.getRGB(x, y)) {
                    return false;
                }
            }
        }
        return true;
    }
}