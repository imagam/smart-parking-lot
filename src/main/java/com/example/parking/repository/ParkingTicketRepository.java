package com.example.parking.repository;

import com.example.parking.entity.ParkingTicket;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ParkingTicketRepository extends JpaRepository<ParkingTicket, Long> {
    @Query("select t from ParkingTicket t join fetch t.vehicle join fetch t.spot where t.id = :id")
    Optional<ParkingTicket> findDetailedById(@Param("id") Long id);
    @Query("select t from ParkingTicket t join fetch t.vehicle join fetch t.spot where t.exitTime is null and t.vehicle.id = :vehicleId")
    List<ParkingTicket> findActiveByVehicleId(@Param("vehicleId") Long vehicleId);
    @Query("select t from ParkingTicket t join fetch t.vehicle join fetch t.spot where t.exitTime is null order by t.entryTime")
    List<ParkingTicket> findAllActive();
}
