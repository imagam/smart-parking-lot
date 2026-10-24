package com.example.parking.repository;

import com.example.parking.entity.ParkingSpot;
import com.example.parking.entity.SpotStatus;
import com.example.parking.entity.VehicleType;
import java.util.List;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface ParkingSpotRepository extends JpaRepository<ParkingSpot, Long> {
    @Query("select s.id from ParkingSpot s where s.status = :available and s.capacity >= :requiredSize order by s.capacity asc, s.floorNumber asc, s.spotNumber asc")
    List<Long> findCandidateIds(@Param("available") SpotStatus available, @Param("requiredSize") int requiredSize);

    // This conditional UPDATE is the atomic claim. Only one concurrent transaction can change AVAILABLE to OCCUPIED.
    @Modifying(flushAutomatically = true)
    @Query("update ParkingSpot s set s.status = :occupied where s.id = :id and s.status = :available")
    int tryOccupy(@Param("id") Long id, @Param("available") SpotStatus available, @Param("occupied") SpotStatus occupied);

    @Modifying(flushAutomatically = true)
    @Query("update ParkingSpot s set s.status = :available where s.id = :id and s.status = :occupied")
    int markAvailable(@Param("id") Long id, @Param("occupied") SpotStatus occupied, @Param("available") SpotStatus available);

    long countByStatus(SpotStatus status);
    long countByStatusAndType(SpotStatus status, VehicleType type);
}
