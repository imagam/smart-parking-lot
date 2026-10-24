package com.example.parking.controller;

import com.example.parking.dto.*;
import com.example.parking.entity.ParkingSpot;
import com.example.parking.entity.ParkingTicket;
import com.example.parking.service.ParkingService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ParkingController {
    private final ParkingService parkingService;
    public ParkingController(ParkingService parkingService) { this.parkingService = parkingService; }

    @PostMapping("/spots")
    @ResponseStatus(HttpStatus.CREATED)
    public SpotResponse createSpot(@Valid @RequestBody CreateSpotRequest request) {
        return SpotResponse.from(parkingService.createSpot(request));
    }

    @GetMapping("/spots")
    public List<SpotResponse> spots(@RequestParam(defaultValue = "false") boolean availableOnly) {
        List<ParkingSpot> spots = availableOnly ? parkingService.listAvailableSpots() : parkingService.listSpots();
        return spots.stream().map(SpotResponse::from).toList();
    }

    @GetMapping("/availability")
    public Map<String, Object> availability() {
        List<SpotResponse> spots = parkingService.listSpots().stream().map(SpotResponse::from).toList();
        long available = spots.stream().filter(s -> s.status().name().equals("AVAILABLE")).count();
        return Map.of("availableSpots", available, "totalSpots", spots.size(), "spots", spots);
    }

    @PostMapping("/parking/check-in")
    @ResponseStatus(HttpStatus.CREATED)
    public TicketResponse checkIn(@Valid @RequestBody CheckInRequest request) {
        return TicketResponse.from(parkingService.checkIn(request));
    }

    @PostMapping("/parking/{ticketId}/check-out")
    public TicketResponse checkOut(@PathVariable Long ticketId) {
        return TicketResponse.from(parkingService.checkOut(ticketId));
    }

    @GetMapping("/parking/active")
    public List<TicketResponse> activeTickets() {
        return parkingService.activeTickets().stream().map(TicketResponse::from).toList();
    }
}
