package com.example.parking.dto;
import com.example.parking.entity.VehicleType;
import jakarta.validation.constraints.*;
public record CreateSpotRequest(@Min(0) int floorNumber, @NotBlank String spotNumber, @NotNull VehicleType type) {}
