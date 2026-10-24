package com.example.parking.dto;
import com.example.parking.entity.ParkingSpot;
import com.example.parking.entity.SpotStatus;
import com.example.parking.entity.VehicleType;
public record SpotResponse(Long id, int floorNumber, String spotNumber, VehicleType type, SpotStatus status) {
    public static SpotResponse from(ParkingSpot s) { return new SpotResponse(s.getId(), s.getFloorNumber(), s.getSpotNumber(), s.getType(), s.getStatus()); }
}
