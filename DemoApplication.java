package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot Application Entry Point.
 * Serves the static Artistic UPI QR Studio frontend and REST security API.
 */
@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
        System.out.println("=========================================================");
        System.out.println("🚀 My-Security-Project Started Successfully!");
        System.out.println("👉 Open your browser at: http://localhost:8080");
        System.out.println("=========================================================");
    }
}
