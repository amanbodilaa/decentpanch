package com.project.decentpanch.controller;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.decentpanch.DTOs.ErrorResponser;
import com.project.decentpanch.entity.*;
import com.project.decentpanch.exceptions.CertificateNotFoundException;
import com.project.decentpanch.exceptions.InvalidCertificateStatusException;
import com.project.decentpanch.service.CertificateService;
import com.project.decentpanch.service.PanchayatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

@RestController
public class PanchayatController {

    @Autowired
    PanchayatService panchayatService;

    @Autowired
    CertificateService certificateService;

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping(value = "panchayat/add/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addPanchayat(
            @RequestPart("panchayat") String panchayatJson,
            @RequestPart("logoImage") MultipartFile logoImage,
            @PathVariable Long userId) {

        try {
            Panchayat panchayat = new ObjectMapper().readValue(panchayatJson, Panchayat.class);
            Panchayat saved = panchayatService.addPanchayatDetails(panchayat, logoImage, userId);
            return ResponseEntity.ok(saved);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Invalid JSON format for Panchayat data.");
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }

    @PreAuthorize("hasRole('ROLE_PANCHAYAT')")
    @GetMapping("/certificate/pending")
    public ResponseEntity<?> getPendingCertificates() {
        try {
            List<Certificate> pendingCertificates = certificateService.getPendingCertificates();
            if (pendingCertificates.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No pending certificates found.");
            }
            return ResponseEntity.ok(pendingCertificates);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching pending certificates: " + e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ROLE_PANCHAYAT')")
    @PostMapping("/certificate/{id}/approve")
    public ResponseEntity<?> approveCertificate(@PathVariable Long id) {
        try {
            Certificate approvedCertificate = certificateService.approveCertificate(id);
            return new ResponseEntity<>(approvedCertificate, HttpStatus.OK);
        } catch (CertificateNotFoundException e) {
            return new ResponseEntity<>(new ErrorResponser("Certificate Not Found", e.getMessage()), HttpStatus.NOT_FOUND);
        } catch (InvalidCertificateStatusException e) {
            return new ResponseEntity<>(new ErrorResponser("Invalid Certificate Status", e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(new ErrorResponser("Internal Server Error", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("hasRole('ROLE_PANCHAYAT')")
    @PostMapping("/certificate/{id}/reject")
    public ResponseEntity<?> rejectCertificate(@PathVariable Long id) {
        try {
            Certificate rejected = certificateService.rejectCertificate(id);
            return ResponseEntity.ok(rejected);
        } catch (CertificateNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponser("Not Found", e.getMessage()));
        } catch (InvalidCertificateStatusException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponser("Invalid Status", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponser("Error", "Something went wrong"));
        }
    }

    @PreAuthorize("hasRole('ROLE_PANCHAYAT')")
    @PostMapping("/certificate/{id}")
    public ResponseEntity<?> getCertificateDetails(@PathVariable Long id) {
        try {
            Certificate certificate = certificateService.findCertificateById(id);
            return ResponseEntity.ok(certificate);
        } catch (CertificateNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponser("Not Found", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponser("Error", "An unexpected error occurred"));
        }
    }





//    Request DEMO
//    {
//        "panchayatName": "Greenfield Panchayat",
//            "panchayatAddress": "123 Main Street, Greenfield Village",
//            "panchayatContact": "9876543210",
//            "userId": 2
//    }




}
