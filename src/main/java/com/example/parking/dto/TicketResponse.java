package com.example.parking.dto;
import com.example.parking.entity.ParkingTicket;
import java.math.BigDecimal;
import java.time.Instant;
public record TicketResponse(Long ticketId, String registrationNumber, String vehicleType, Long spotId, int floorNumber, String spotNumber, Instant entryTime, Instant exitTime, BigDecimal fee, boolean active) {
    public static TicketResponse from(ParkingTicket t) {
        return new TicketResponse(t.getId(), t.getVehicle().getRegistrationNumber(), t.getVehicle().getType().name(), t.getSpot().getId(), t.getSpot().getFloorNumber(), t.getSpot().getSpotNumber(), t.getEntryTime(), t.getExitTime(), t.getFee(), t.isActive());
    }
}
