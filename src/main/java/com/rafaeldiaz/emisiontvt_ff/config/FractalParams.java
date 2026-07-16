package com.rafaeldiaz.emisiontvt_ff.config;

import lombok.Data;
import java.util.List;

@Data
public class FractalParams {
    private double startRe;
    private double startIm;
    private double startZoom;
    private double targetRe;
    private double targetIm;
    private double targetZoom;
    private List<String> palette; // lista de colores HEX
}