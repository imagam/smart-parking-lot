package com.example.parking.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "parking_spots", uniqueConstraints = @UniqueConstraint(name = "uk_floor_spot_number", columnNames = {"floor_number", "spot_number"}))
public class ParkingSpot {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "floor_number", nullable = false) private int floorNumber;
    @Column(name = "spot_number", nullable = false, length = 30) private String spotNumber;
    @Enumerated(EnumType.STRING) @Column(name = "spot_type", nullable = false, length = 20) private VehicleType type;
    @Column(nullable = false) private int capacity;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private SpotStatus status = SpotStatus.AVAILABLE;

    protected ParkingSpot() {}
    public ParkingSpot(int floorNumber, String spotNumber, VehicleType type) {
        if (floorNumber < 0) throw new IllegalArgumentException("Floor number cannot be negative");
        this.floorNumber = floorNumber;
        this.spotNumber = spotNumber.trim();
        this.type = type;
        this.capacity = type.size();
    }
    public Long getId() { return id; }
    public int getFloorNumber() { return floorNumber; }
    public String getSpotNumber() { return spotNumber; }
    public VehicleType getType() { return type; }
    public SpotStatus getStatus() { return status; }
}
