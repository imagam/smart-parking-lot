package com.example.parking.dto;
import com.example.parking.entity.VehicleType;
import jakarta.validation.constraints.*;
public record CheckInRequest(@NotBlank @Size(max=30) String registrationNumber, @NotNull VehicleType type) {}
