package com.example.parking.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "parking_tickets", indexes = {
    @Index(name = "idx_ticket_vehicle_active", columnList = "vehicle_id, exit_time"),
    @Index(name = "idx_ticket_spot_active", columnList = "spot_id, exit_time")
})
public class ParkingTicket {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY) @JoinColumn(name = "vehicle_id", nullable = false) private Vehicle vehicle;
    @ManyToOne(optional = false, fetch = FetchType.LAZY) @JoinColumn(name = "spot_id", nullable = false) private ParkingSpot spot;
    @Column(name = "entry_time", nullable = false) private Instant entryTime;
    @Column(name = "exit_time") private Instant exitTime;
    @Column(name = "fee", precision = 12, scale = 2) private BigDecimal fee;

    protected ParkingTicket() {}
    public ParkingTicket(Vehicle vehicle, ParkingSpot spot, Instant entryTime) {
        this.vehicle = vehicle; this.spot = spot; this.entryTime = entryTime;
    }
    public void close(Instant exitTime, BigDecimal fee) {
        if (this.exitTime != null) throw new IllegalStateException("Ticket is already closed");
        if (exitTime.isBefore(entryTime)) throw new IllegalArgumentException("Exit time cannot precede entry time");
        this.exitTime = exitTime; this.fee = fee;
    }
    public Long getId() { return id; }
    public Vehicle getVehicle() { return vehicle; }
    public ParkingSpot getSpot() { return spot; }
    public Instant getEntryTime() { return entryTime; }
    public Instant getExitTime() { return exitTime; }
    public BigDecimal getFee() { return fee; }
    public boolean isActive() { return exitTime == null; }
}
