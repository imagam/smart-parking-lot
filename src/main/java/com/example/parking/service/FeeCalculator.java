package com.example.parking.service;

import com.example.parking.entity.VehicleType;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class FeeCalculator {
    // Demo tariff: charged per started hour; minimum charge is one hour.
    public BigDecimal calculate(VehicleType type, Instant entry, Instant exit) {
        if (exit.isBefore(entry)) throw new IllegalArgumentException("Exit time cannot precede entry time");
        long seconds = Duration.between(entry, exit).getSeconds();
        long hours = Math.max(1, (seconds + 3599) / 3600);
        BigDecimal hourlyRate = switch (type) {
            case MOTORCYCLE -> new BigDecimal("20.00");
            case CAR -> new BigDecimal("50.00");
            case BUS -> new BigDecimal("100.00");
        };
        return hourlyRate.multiply(BigDecimal.valueOf(hours)).setScale(2, RoundingMode.HALF_UP);
    }
}
