package com.example.parking.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import com.example.parking.entity.VehicleType;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class FeeCalculatorTest {
    private final FeeCalculator calculator = new FeeCalculator();

    @Test void chargesMinimumOneHourForShortStay() {
        assertEquals(new BigDecimal("50.00"), calculator.calculate(VehicleType.CAR,
                Instant.parse("2026-01-01T10:00:00Z"), Instant.parse("2026-01-01T10:10:00Z")));
    }
    @Test void roundsPartialHourUp() {
        assertEquals(new BigDecimal("100.00"), calculator.calculate(VehicleType.CAR,
                Instant.parse("2026-01-01T10:00:00Z"), Instant.parse("2026-01-01T11:01:00Z")));
    }
    @Test void chargesNextHourWhenStayExceedsAnHourByOneSecond() {
        assertEquals(new BigDecimal("100.00"), calculator.calculate(VehicleType.CAR,
                Instant.parse("2026-01-01T10:00:00Z"), Instant.parse("2026-01-01T11:00:01Z")));
    }
    @Test void usesVehicleSpecificRate() {
        assertEquals(new BigDecimal("100.00"), calculator.calculate(VehicleType.BUS,
                Instant.parse("2026-01-01T10:00:00Z"), Instant.parse("2026-01-01T11:00:00Z")));
    }
}
