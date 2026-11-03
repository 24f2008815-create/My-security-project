package com.example.demo.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * REST Controller for Gmail Authentication, User Registration,
 * and Web CSV Batch processing.
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class AuthController {

    private final PasswordEncoder passwordEncoder;

    // Thread-safe in-memory User Database & Security Audit Registry
    private final Map<String, UserRecord> userDatabase = new ConcurrentHashMap<>();

    // Sample In-Memory CSV Batch Payment Database (from Colab Cell 6)
    private final List<Map<String, Object>> batchRecords = Collections.synchronizedList(new ArrayList<>());

    public AuthController(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;

        // Seed with primary merchant account
        String defaultEmail = "vamikayadav30122015@gmail.com";
        userDatabase.put(defaultEmail.toLowerCase(), new UserRecord(
                defaultEmail,
                passwordEncoder.encode("SecurePass123!"),
                "Verified Merchant",
                Instant.now().toString()
        ));

        // Seed sample UPI records
        addBatchRecord("852753naveen-1@okaxis", "Naveen Singh", 501.00, "Shiv Arpan");
        addBatchRecord("temple@upi", "Kashi Vishwanath Mandir", 1008.00, "Temple Seva");
        addBatchRecord("ganeshstore@okicici", "Shree Ganesh Store", 251.00, "Shubh Labh Donation");
        addBatchRecord("ashramtrust@sbi", "Vedic Wisdom Ashram", 108.00, "Annadanam Seva");
        addBatchRecord("paytm.s3ufuwv@pty", "Paytm Store", 0.00, "Paytm Seva Counter");
    }

    private void addBatchRecord(String upiId, String name, double amount, String note) {
        Map<String, Object> record = new LinkedHashMap<>();
        record.put("upi_id", upiId);
        record.put("payee_name", name);
        record.put("amount", amount);
        record.put("note", note);
        batchRecords.add(record);
    }

    // ==================== AUTHENTICATION ENDPOINTS ====================

    @PostMapping("/auth/register")
    public ResponseEntity<?> registerUser(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");

        if (email == null || !email.trim().toLowerCase().endsWith("@gmail.com")) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "A valid Gmail address (@gmail.com) is strictly required."
            ));
        }

        if (password == null || password.length() < 6) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Password must be at least 6 characters."
            ));
        }

        String lowerEmail = email.trim().toLowerCase();
        if (userDatabase.containsKey(lowerEmail)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "status", "error",
                    "message", "Gmail account already registered. Please sign in."
            ));
        }

        // Hash with BCrypt (12 rounds)
        String hashedPassword = passwordEncoder.encode(password);
        UserRecord user = new UserRecord(email, hashedPassword, "Registered Merchant", Instant.now().toString());
        userDatabase.put(lowerEmail, user);

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Registration successful via Gmail!",
                "email", email,
                "role", user.role()
        ));
    }

    @PostMapping("/auth/login")
    public ResponseEntity<?> loginUser(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");

        if (email == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Email and password are required."
            ));
        }

        UserRecord user = userDatabase.get(email.trim().toLowerCase());
        if (user == null || !passwordEncoder.matches(password, user.passwordHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "status", "error",
                    "message", "Invalid Gmail address or password."
            ));
        }

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Signed in securely!",
                "email", user.email(),
                "role", user.role()
        ));
    }

    // ==================== CSV BATCH ENDPOINTS ====================

    @GetMapping(value = "/csv/export-users", produces = "text/csv")
    public ResponseEntity<String> exportUsersCsv() {
        StringBuilder csv = new StringBuilder();
        csv.append("gmail_address,role,registered_timestamp,bcrypt_hash_sample\n");

        for (UserRecord u : userDatabase.values()) {
            csv.append(String.format("\"%s\",\"%s\",\"%s\",\"%s\"\n",
                    u.email(), u.role(), u.registeredAt(), u.passwordHash().substring(0, 20) + "..."));
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"gmail_security_registry.csv\"")
                .body(csv.toString());
    }

    @GetMapping(value = "/csv/template", produces = "text/csv")
    public ResponseEntity<String> downloadCsvTemplate() {
        StringBuilder csv = new StringBuilder();
        csv.append("upi_id,payee_name,amount,note\n");

        for (Map<String, Object> r : batchRecords) {
            csv.append(String.format("\"%s\",\"%s\",%s,\"%s\"\n",
                    r.get("upi_id"), r.get("payee_name"), r.get("amount"), r.get("note")));
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"upi_batch_template.csv\"")
                .body(csv.toString());
    }

    @PostMapping("/csv/upload")
    public ResponseEntity<?> uploadBatchCsv(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "CSV file is empty."));
        }

        List<Map<String, Object>> importedList = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "File has no header row."));
            }

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] cols = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                if (cols.length >= 2) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("upi_id", cols[0].replaceAll("^\"|\"$", "").trim());
                    row.put("payee_name", cols.length > 1 ? cols[1].replaceAll("^\"|\"$", "").trim() : "Payee");
                    row.put("amount", cols.length > 2 ? Double.parseDouble(cols[2].replaceAll("^\"|\"$", "").trim()) : 0.0);
                    row.put("note", cols.length > 3 ? cols[3].replaceAll("^\"|\"$", "").trim() : "");
                    importedList.add(row);
                }
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "error", "message", "Failed to parse CSV: " + e.getMessage()));
        }

        batchRecords.addAll(importedList);
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "imported_count", importedList.size(),
                "total_records", batchRecords.size()
        ));
    }

    public record UserRecord(String email, String passwordHash, String role, String registeredAt) {}
}
