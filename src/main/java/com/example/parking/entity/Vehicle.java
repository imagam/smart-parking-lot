package com.example.parking.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "vehicles", uniqueConstraints = @UniqueConstraint(name = "uk_vehicle_registration", columnNames = "registration_number"))
public class Vehicle {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "registration_number", nullable = false, unique = true, length = 30)
    private String registrationNumber;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private VehicleType type;

    protected Vehicle() {}
    public Vehicle(String registrationNumber, VehicleType type) {
        this.registrationNumber = registrationNumber.trim().toUpperCase();
        this.type = type;
    }
    public Long getId() { return id; }
    public String getRegistrationNumber() { return registrationNumber; }
    public VehicleType getType() { return type; }
}
