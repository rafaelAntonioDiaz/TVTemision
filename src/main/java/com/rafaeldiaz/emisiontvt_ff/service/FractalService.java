package com.rafaeldiaz.emisiontvt_ff.service;

import com.rafaeldiaz.emisiontvt_ff.config.FractalParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;

import tools.jackson.databind.ObjectMapper;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Service
public class FractalService {
    private final ObjectMapper mapper = new ObjectMapper();
    @Value("${app.upload.fractals.dir}")
    private String uploadDir;

    public FractalParams generateParams(Long investorId, String tokenCode, String roundName) {
        String seed = investorId + ":" + tokenCode;
        long hash = seed.hashCode();
        Random rand = new Random(hash);

        // Punto más hermoso del conjunto (nunca negro)
        double baseRe = -0.7435669;
        double baseIm = 0.1314023;

        // Pequeña variación para cada token (sin perder la belleza)
        double offsetRe = (rand.nextDouble() - 0.5) * 0.0002;   // ±0.0001
        double offsetIm = (rand.nextDouble() - 0.5) * 0.0002;

        double targetRe = baseRe + offsetRe;
        double targetIm = baseIm + offsetIm;

        // Zoom final entre 500 y 2000 (detalle sin perder color)
        double targetZoom = 500 + rand.nextDouble() * 1500;

        FractalParams params = new FractalParams();
        params.setStartRe(targetRe);     // mismo centro
        params.setStartIm(targetIm);
        params.setStartZoom(1.5);        // vista amplia alrededor
        params.setTargetRe(targetRe);
        params.setTargetIm(targetIm);
        params.setTargetZoom(targetZoom);
        params.setPalette(generateGradientPalette(roundName, rand));
        return params;
    }
    
    public BufferedImage renderStatic(FractalParams params, int width, int height) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        double size = 3.5 / params.getTargetZoom();
        double reMin = params.getTargetRe() - size / 2;
        double imMin = params.getTargetIm() - size / 2;
        int maxIter = 300;

        // Convertir paleta HEX a Color[]
        Color[] palette = params.getPalette().stream()
                .map(hex -> Color.decode(hex))
                .toArray(Color[]::new);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                double cRe = reMin + x * size / width;
                double cIm = imMin + y * size / height;
                double zRe = 0, zIm = 0;
                int iter = 0;
                while (zRe * zRe + zIm * zIm <= 4 && iter < maxIter) {
                    double tmp = zRe * zRe - zIm * zIm + cRe;
                    zIm = 2.0 * zRe * zIm + cIm;
                    zRe = tmp;
                    iter++;
                }
                Color color = iter == maxIter ? Color.BLACK : palette[iter % palette.length];
                img.setRGB(x, y, color.getRGB());
            }
        }
        return img;
    }

    public String saveFractalImage(BufferedImage img, String tokenCode) throws IOException {
        Files.createDirectories(Paths.get(uploadDir));
        String fileName = tokenCode + ".png";
        File outputFile = new File(uploadDir + fileName);
        ImageIO.write(img, "png", outputFile);
        return outputFile.getAbsolutePath();
    }

    public String toJson(FractalParams params) {
        try {
            return mapper.writeValueAsString(params);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public FractalParams fromJson(String json) {
        try {
            return mapper.readValue(json, FractalParams.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private List<String> generateGradientPalette(String roundName, Random rand) {
        return switch (roundName) {
            case "F&F" -> gradient("#0d1b2a", "#1b263b", "#415a77", "#778da9", "#e0e1dd",
                    "#f4a261", "#e76f51", "#e9c46a", "#ffb703");
            case "HITO1" -> gradient("#0b090a", "#161a1d", "#660708", "#a4161a",
                    "#e5383b", "#ba1826");
            case "HITO2" -> gradient("#03071e", "#370617", "#6a040f", "#9d0208",
                    "#d00000", "#ffba08", "#e85d04");
            case "CIRCULO" -> gradient("#10002b", "#240046", "#3c096c", "#5a189a",
                    "#9d4edd", "#c77dff", "#e0aaff");
            default -> gradient("#1a1a2e", "#16213e", "#0f3460", "#e94560");
        };
    }

    // Genera una lista de 256 colores interpolando linealmente entre los dados
    private List<String> gradient(String... colors) {
        List<int[]> rgbs = new ArrayList<>();
        for (String c : colors) {
            rgbs.add(new int[]{
                    Integer.parseInt(c.substring(1,3), 16),
                    Integer.parseInt(c.substring(3,5), 16),
                    Integer.parseInt(c.substring(5,7), 16)
            });
        }
        List<String> palette = new ArrayList<>(256);
        int steps = 256;
        int segs = colors.length - 1;
        for (int i = 0; i < steps; i++) {
            double t = (double) i / (steps - 1) * segs;
            int seg = Math.min((int) t, segs - 1);
            double local = t - seg;
            int[] c1 = rgbs.get(seg);
            int[] c2 = rgbs.get(seg + 1);
            int r = (int)(c1[0] + (c2[0] - c1[0]) * local);
            int g = (int)(c1[1] + (c2[1] - c1[1]) * local);
            int b = (int)(c1[2] + (c2[2] - c1[2]) * local);
            palette.add(String.format("#%02x%02x%02x", r, g, b));
        }
        return palette;
    }
}