package com.transitops;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/trips")
public class TripController {
    private final TripRepository tripRepository;
    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;

    // Manual constructor
    public TripController(TripRepository tripRepository, VehicleRepository vehicleRepository, DriverRepository driverRepository) {
        this.tripRepository = tripRepository;
        this.vehicleRepository = vehicleRepository;
        this.driverRepository = driverRepository;
    }

    @GetMapping
    public ResponseEntity<List<Trip>> getAllTrips() {
        return ResponseEntity.ok(tripRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Trip> getTrip(@PathVariable Long id) {
        return tripRepository.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createTrip(@RequestBody Trip trip) {
        Vehicle vehicle = vehicleRepository.findById(trip.getVehicleId())
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));
        if (vehicle.getStatus() != Vehicle.VehicleStatus.AVAILABLE) {
            return ResponseEntity.badRequest().body(Map.of("error", "Vehicle is not available"));
        }
        if (trip.getCargoWeight() > vehicle.getMaxLoadCapacity()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Cargo weight exceeds capacity"));
        }
        Driver driver = driverRepository.findById(trip.getDriverId())
                .orElseThrow(() -> new RuntimeException("Driver not found"));
        if (driver.getStatus() != Driver.DriverStatus.AVAILABLE) {
            return ResponseEntity.badRequest().body(Map.of("error", "Driver is not available"));
        }
        trip.setStatus(Trip.TripStatus.DRAFT);
        return ResponseEntity.ok(tripRepository.save(trip));
    }

    @PostMapping("/{id}/dispatch")
    public ResponseEntity<?> dispatchTrip(@PathVariable Long id) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Trip not found"));
        if (trip.getStatus() != Trip.TripStatus.DRAFT) {
            return ResponseEntity.badRequest().body(Map.of("error", "Only draft trips can be dispatched"));
        }
        Vehicle vehicle = vehicleRepository.findById(trip.getVehicleId())
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));
        if (vehicle.getStatus() != Vehicle.VehicleStatus.AVAILABLE) {
            return ResponseEntity.badRequest().body(Map.of("error", "Vehicle is not available"));
        }
        Driver driver = driverRepository.findById(trip.getDriverId())
                .orElseThrow(() -> new RuntimeException("Driver not found"));
        if (driver.getStatus() != Driver.DriverStatus.AVAILABLE) {
            return ResponseEntity.badRequest().body(Map.of("error", "Driver is not available"));
        }
        vehicle.setStatus(Vehicle.VehicleStatus.ON_TRIP);
        vehicleRepository.save(vehicle);
        driver.setStatus(Driver.DriverStatus.ON_TRIP);
        driverRepository.save(driver);
        trip.setStatus(Trip.TripStatus.DISPATCHED);
        trip.setDispatchedAt(LocalDateTime.now());
        tripRepository.save(trip);
        return ResponseEntity.ok(Map.of("message", "Trip dispatched successfully"));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<?> completeTrip(@PathVariable Long id, @RequestParam Double actualDistance) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Trip not found"));
        if (trip.getStatus() != Trip.TripStatus.DISPATCHED) {
            return ResponseEntity.badRequest().body(Map.of("error", "Only dispatched trips can be completed"));
        }
        trip.setStatus(Trip.TripStatus.COMPLETED);
        trip.setActualDistance(actualDistance);
        trip.setCompletedAt(LocalDateTime.now());
        tripRepository.save(trip);
        Vehicle vehicle = vehicleRepository.findById(trip.getVehicleId()).orElse(null);
        if (vehicle != null) {
            vehicle.setStatus(Vehicle.VehicleStatus.AVAILABLE);
            vehicleRepository.save(vehicle);
        }
        Driver driver = driverRepository.findById(trip.getDriverId()).orElse(null);
        if (driver != null) {
            driver.setStatus(Driver.DriverStatus.AVAILABLE);
            driver.setTripsCompleted(driver.getTripsCompleted() + 1);
            driverRepository.save(driver);
        }
        return ResponseEntity.ok(Map.of("message", "Trip completed successfully"));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancelTrip(@PathVariable Long id) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Trip not found"));
        if (trip.getStatus() == Trip.TripStatus.COMPLETED) {
            return ResponseEntity.badRequest().body(Map.of("error", "Completed trips cannot be cancelled"));
        }
        if (trip.getStatus() == Trip.TripStatus.DISPATCHED) {
            Vehicle vehicle = vehicleRepository.findById(trip.getVehicleId()).orElse(null);
            if (vehicle != null) {
                vehicle.setStatus(Vehicle.VehicleStatus.AVAILABLE);
                vehicleRepository.save(vehicle);
            }
            Driver driver = driverRepository.findById(trip.getDriverId()).orElse(null);
            if (driver != null) {
                driver.setStatus(Driver.DriverStatus.AVAILABLE);
                driverRepository.save(driver);
            }
        }
        trip.setStatus(Trip.TripStatus.CANCELLED);
        tripRepository.save(trip);
        return ResponseEntity.ok(Map.of("message", "Trip cancelled successfully"));
    }

    @GetMapping("/live-board")
    public ResponseEntity<List<Trip>> getLiveBoard() {
        return ResponseEntity.ok(tripRepository.findByStatusIn(List.of(Trip.TripStatus.DRAFT, Trip.TripStatus.DISPATCHED)));
    }
}
