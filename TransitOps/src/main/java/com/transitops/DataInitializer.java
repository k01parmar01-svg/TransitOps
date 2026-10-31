package com.transitops;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.time.LocalDate;

@Component
public class DataInitializer implements CommandLineRunner {
    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Constructor
    public DataInitializer(VehicleRepository vehicleRepository, DriverRepository driverRepository, 
                           UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.vehicleRepository = vehicleRepository;
        this.driverRepository = driverRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Create users with different roles
        if (userRepository.count() == 0) {
            // Admin User
            User admin = new User();
            admin.setEmail("admin@transitops.com");
            admin.setPasswordHash(passwordEncoder.encode("admin123"));
            admin.setName("System Admin");
            admin.setDepartment("IT");
            admin.getRoles().add("ADMIN");
            admin.getRoles().add("FLEET_MANAGER");
            admin.getRoles().add("DISPATCHER");
            admin.getRoles().add("SAFETY_OFFICER");
            admin.getRoles().add("FINANCIAL_ANALYST");
            userRepository.save(admin);
            System.out.println("? Admin user created: admin@transitops.com / admin123");

            // Fleet Manager
            User fleetManager = new User();
            fleetManager.setEmail("fleet@transitops.in");
            fleetManager.setPasswordHash(passwordEncoder.encode("fleet123"));
            fleetManager.setName("Rahul Sharma");
            fleetManager.setDepartment("Fleet Management");
            fleetManager.getRoles().add("FLEET_MANAGER");
            userRepository.save(fleetManager);
            System.out.println("? Fleet Manager created: fleet@transitops.in / fleet123");

            // Dispatcher
            User dispatcher = new User();
            dispatcher.setEmail("dispatch@transitops.in");
            dispatcher.setPasswordHash(passwordEncoder.encode("dispatch123"));
            dispatcher.setName("Priya Patel");
            dispatcher.setDepartment("Dispatch");
            dispatcher.getRoles().add("DISPATCHER");
            userRepository.save(dispatcher);
            System.out.println("? Dispatcher created: dispatch@transitops.in / dispatch123");

            // Safety Officer
            User safetyOfficer = new User();
            safetyOfficer.setEmail("safety@transitops.in");
            safetyOfficer.setPasswordHash(passwordEncoder.encode("safety123"));
            safetyOfficer.setName("Vikram Singh");
            safetyOfficer.setDepartment("Safety");
            safetyOfficer.getRoles().add("SAFETY_OFFICER");
            userRepository.save(safetyOfficer);
            System.out.println("? Safety Officer created: safety@transitops.in / safety123");

            // Financial Analyst
            User financialAnalyst = new User();
            financialAnalyst.setEmail("finance@transitops.in");
            financialAnalyst.setPasswordHash(passwordEncoder.encode("finance123"));
            financialAnalyst.setName("Anjali Reddy");
            financialAnalyst.setDepartment("Finance");
            financialAnalyst.getRoles().add("FINANCIAL_ANALYST");
            userRepository.save(financialAnalyst);
            System.out.println("? Financial Analyst created: finance@transitops.in / finance123");
        }

        // Add sample vehicles if none exist
        if (vehicleRepository.count() == 0) {
            Vehicle v1 = new Vehicle();
            v1.setRegistrationNumber("6J01AB4521");
            v1.setName("VAN-05");
            v1.setModel("Toyota Hiace");
            v1.setType(Vehicle.VehicleType.VAN);
            v1.setMaxLoadCapacity(500.0);
            v1.setOdometer(74000.0);
            v1.setAcquisitionCost(2450000.0);
            v1.setStatus(Vehicle.VehicleStatus.AVAILABLE);
            vehicleRepository.save(v1);

            Vehicle v2 = new Vehicle();
            v2.setRegistrationNumber("6J01AB4198");
            v2.setName("TRUCK-11");
            v2.setModel("Tata Ace");
            v2.setType(Vehicle.VehicleType.TRUCK);
            v2.setMaxLoadCapacity(5000.0);
            v2.setOdometer(182000.0);
            v2.setAcquisitionCost(4500000.0);
            v2.setStatus(Vehicle.VehicleStatus.AVAILABLE);
            vehicleRepository.save(v2);

            Vehicle v3 = new Vehicle();
            v3.setRegistrationNumber("6J01AB1120");
            v3.setName("MINI-03");
            v3.setModel("Maruti Suzuki");
            v3.setType(Vehicle.VehicleType.MINI);
            v3.setMaxLoadCapacity(1000.0);
            v3.setOdometer(66000.0);
            v3.setAcquisitionCost(410000.0);
            v3.setStatus(Vehicle.VehicleStatus.AVAILABLE);
            vehicleRepository.save(v3);

            System.out.println("? Sample vehicles added!");
        }

        // Add sample drivers if none exist
        if (driverRepository.count() == 0) {
            Driver d1 = new Driver();
            d1.setName("Alex Driver");
            d1.setLicenseNumber("DL-8842B");
            d1.setLicenseCategory("LMV");
            d1.setLicenseExpiryDate(LocalDate.of(2028, 12, 31));
            d1.setContactNumber("9876543210");
            d1.setSafetyScore(96);
            d1.setStatus(Driver.DriverStatus.AVAILABLE);
            driverRepository.save(d1);

            Driver d2 = new Driver();
            d2.setName("Priya Singh");
            d2.setLicenseNumber("DL-77031");
            d2.setLicenseCategory("LMV");
            d2.setLicenseExpiryDate(LocalDate.of(2026, 8, 20));
            d2.setContactNumber("9911044444");
            d2.setSafetyScore(99);
            d2.setStatus(Driver.DriverStatus.AVAILABLE);
            driverRepository.save(d2);

            System.out.println("? Sample drivers added!");
        }
    }
}
