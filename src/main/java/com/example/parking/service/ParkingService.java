package com.example.parking.service;

import com.example.parking.dto.CheckInRequest;
import com.example.parking.dto.CreateSpotRequest;
import com.example.parking.entity.*;
import com.example.parking.exception.ConflictException;
import com.example.parking.exception.NotFoundException;
import com.example.parking.repository.*;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ParkingService {
    private final VehicleRepository vehicleRepository;
    private final ParkingSpotRepository spotRepository;
    private final ParkingTicketRepository ticketRepository;
    private final FeeCalculator feeCalculator;

    public ParkingService(VehicleRepository vehicleRepository, ParkingSpotRepository spotRepository,
                          ParkingTicketRepository ticketRepository, FeeCalculator feeCalculator) {
        this.vehicleRepository = vehicleRepository;
        this.spotRepository = spotRepository;
        this.ticketRepository = ticketRepository;
        this.feeCalculator = feeCalculator;
    }

    @Transactional
    public ParkingSpot createSpot(CreateSpotRequest request) {
        return spotRepository.save(new ParkingSpot(request.floorNumber(), request.spotNumber(), request.type()));
    }

    @Transactional(readOnly = true)
    public List<ParkingSpot> listSpots() { return spotRepository.findAll(); }

    @Transactional(readOnly = true)
    public List<ParkingSpot> listAvailableSpots() {
        return spotRepository.findAll().stream().filter(s -> s.getStatus() == SpotStatus.AVAILABLE).toList();
    }

    @Transactional
    public ParkingTicket checkIn(CheckInRequest request) {
        String registration = request.registrationNumber().trim().toUpperCase();
        Vehicle vehicle = vehicleRepository.findByRegistrationNumberForUpdate(registration)
                .orElseGet(() -> vehicleRepository.save(new Vehicle(registration, request.type())));
        if (vehicle.getType() != request.type()) {
            throw new ConflictException("Registration number is already registered with vehicle type " + vehicle.getType());
        }
        if (!ticketRepository.findActiveByVehicleId(vehicle.getId()).isEmpty()) {
            throw new ConflictException("This vehicle already has an active parking ticket");
        }

        // Smallest compatible spot first; floor and spot number provide deterministic tie-breaking.
        // The SELECT only discovers candidates. The conditional UPDATE is the concurrency-safe claim.
        List<Long> candidates = spotRepository.findCandidateIds(SpotStatus.AVAILABLE, request.type().size());
        for (Long spotId : candidates) {
            int claimed = spotRepository.tryOccupy(spotId, SpotStatus.AVAILABLE, SpotStatus.OCCUPIED);
            if (claimed == 1) {
                ParkingSpot spot = spotRepository.findById(spotId)
                        .orElseThrow(() -> new NotFoundException("Parking spot not found: " + spotId));
                return ticketRepository.save(new ParkingTicket(vehicle, spot, Instant.now()));
            }
        }
        throw new ConflictException("No compatible parking spot is currently available");
    }

    @Transactional
    public ParkingTicket checkOut(Long ticketId) {
        ParkingTicket ticket = ticketRepository.findDetailedById(ticketId)
                .orElseThrow(() -> new NotFoundException("Parking ticket not found: " + ticketId));
        if (!ticket.isActive()) throw new ConflictException("Parking ticket is already checked out");
        Instant exitTime = Instant.now();
        ticket.close(exitTime, feeCalculator.calculate(ticket.getVehicle().getType(), ticket.getEntryTime(), exitTime));
        int released = spotRepository.markAvailable(ticket.getSpot().getId(), SpotStatus.OCCUPIED, SpotStatus.AVAILABLE);
        if (released != 1) throw new ConflictException("Spot state is inconsistent; cannot check out ticket");
        return ticketRepository.save(ticket);
    }

    @Transactional(readOnly = true)
    public List<ParkingTicket> activeTickets() { return ticketRepository.findAllActive(); }
}
