package com.example.parking.entity;

public enum VehicleType {
    MOTORCYCLE(1), CAR(2), BUS(3);
    private final int size;
    VehicleType(int size) { this.size = size; }
    public int size() { return size; }
}
