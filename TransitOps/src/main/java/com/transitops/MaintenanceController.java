package com.transitops;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/maintenance")
public class MaintenanceController {
    private final MaintenanceRepository maintenanceRepository;
    private final VehicleRepository vehicleRepository;

    // Manual constructor
    public MaintenanceController(MaintenanceRepository maintenanceRepository, VehicleRepository vehicleRepository) {
        this.maintenanceRepository = maintenanceRepository;
        this.vehicleRepository = vehicleRepository;
    }

    @GetMapping
    public ResponseEntity<List<Maintenance>> getAllMaintenance() {
        return ResponseEntity.ok(maintenanceRepository.findAll());
    }

    @GetMapping("/vehicle/{vehicleId}")
    public ResponseEntity<List<Maintenance>> getMaintenanceByVehicle(@PathVariable Long vehicleId) {
        return ResponseEntity.ok(maintenanceRepository.findByVehicleId(vehicleId));
    }

    @PostMapping
    public ResponseEntity<?> createMaintenance(@RequestBody Maintenance maintenance) {
        Vehicle vehicle = vehicleRepository.findById(maintenance.getVehicleId())
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));
        if (vehicle.getStatus() == Vehicle.VehicleStatus.RETIRED) {
            return ResponseEntity.badRequest().body(Map.of("error", "Cannot add maintenance to retired vehicle"));
        }
        vehicle.setStatus(Vehicle.VehicleStatus.IN_SHOP);
        vehicleRepository.save(vehicle);
        maintenance.setStatus(Maintenance.MaintenanceStatus.ACTIVE);
        return ResponseEntity.ok(maintenanceRepository.save(maintenance));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<?> completeMaintenance(@PathVariable Long id) {
        Maintenance maintenance = maintenanceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Maintenance record not found"));
        maintenance.setStatus(Maintenance.MaintenanceStatus.COMPLETED);
        maintenance.setCompletedAt(LocalDate.now());
        maintenanceRepository.save(maintenance);
        Vehicle vehicle = vehicleRepository.findById(maintenance.getVehicleId()).orElse(null);
        if (vehicle != null && vehicle.getStatus() != Vehicle.VehicleStatus.RETIRED) {
            vehicle.setStatus(Vehicle.VehicleStatus.AVAILABLE);
            vehicleRepository.save(vehicle);
        }
        return ResponseEntity.ok(Map.of("message", "Maintenance completed"));
    }
}
