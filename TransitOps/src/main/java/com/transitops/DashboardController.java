package com.transitops;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.*;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;
    private final TripRepository tripRepository;

    // Manual constructor
    public DashboardController(VehicleRepository vehicleRepository, DriverRepository driverRepository, TripRepository tripRepository) {
        this.vehicleRepository = vehicleRepository;
        this.driverRepository = driverRepository;
        this.tripRepository = tripRepository;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getDashboard() {
        List<Vehicle> vehicles = vehicleRepository.findAll();
        List<Driver> drivers = driverRepository.findAll();
        List<Trip> trips = tripRepository.findAll();

        long activeVehicles = vehicles.stream().filter(v -> v.getStatus() != Vehicle.VehicleStatus.RETIRED).count();
        long availableVehicles = vehicles.stream().filter(v -> v.getStatus() == Vehicle.VehicleStatus.AVAILABLE).count();
        long inMaintenance = vehicles.stream().filter(v -> v.getStatus() == Vehicle.VehicleStatus.IN_SHOP).count();
        long activeTrips = trips.stream().filter(t -> t.getStatus() == Trip.TripStatus.DISPATCHED).count();
        long driversOnDuty = drivers.stream().filter(d -> d.getStatus() == Driver.DriverStatus.AVAILABLE || d.getStatus() == Driver.DriverStatus.ON_TRIP).count();
        double fleetUtilization = vehicles.isEmpty() ? 0 : (double) activeVehicles / vehicles.size() * 100;

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("activeVehicles", activeVehicles);
        response.put("availableVehicles", availableVehicles);
        response.put("vehiclesInMaintenance", inMaintenance);
        response.put("activeTrips", activeTrips);
        response.put("driversOnDuty", driversOnDuty);
        response.put("fleetUtilization", Math.round(fleetUtilization * 100.0) / 100.0);
        response.put("totalVehicles", vehicles.size());
        response.put("totalDrivers", drivers.size());
        response.put("totalTrips", trips.size());
        return ResponseEntity.ok(response);
    }
}
