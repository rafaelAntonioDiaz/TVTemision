package com.rafaeldiaz.emisiontvt_ff.config;

import com.rafaeldiaz.emisiontvt_ff.entity.Round;
import com.rafaeldiaz.emisiontvt_ff.repository.RoundRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final RoundRepository roundRepo;
    @Override
    public void run(String... args) {
        if (roundRepo.count() == 0) {
            roundRepo.saveAll(List.of(
                    new Round(null, "F&F", BigDecimal.valueOf(500), 5, 20),
                    new Round(null, "HITO1", BigDecimal.valueOf(1000), 8, 24),
                    new Round(null, "HITO2", BigDecimal.valueOf(2000), 10, 30),
                    new Round(null, "CIRCULO", BigDecimal.valueOf(5000), 3, 12)
            ));
        }
    }
}