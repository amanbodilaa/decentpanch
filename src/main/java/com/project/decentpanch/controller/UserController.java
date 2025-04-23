package com.project.decentpanch.controller;

import com.project.decentpanch.entity.Certificate;
import com.project.decentpanch.service.CertificateService;
import com.project.decentpanch.service.PdfGenerationService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;


@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    CertificateService certificateService;

    @Autowired
    PdfGenerationService pdfGenerationService;


    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/certificates/request")
    public ResponseEntity<?> requestCertificate(@RequestBody Certificate certificate) {
        try {
            Certificate savedCertificate = certificateService.addRequest(certificate);
            return ResponseEntity.ok(savedCertificate);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while processing the certificate request: " + e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/certificates/requests/{userId}")
    public ResponseEntity<?> listRequests(@PathVariable Long userId) {
        try {
            List<Certificate> certificates = certificateService.getCertificatesByUserId(userId);
            if (certificates.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("No certificate requests found for user with ID: " + userId);
            }
            return ResponseEntity.ok(certificates);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while fetching certificate requests: " + e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/certificate/{id}/download")
    public ResponseEntity<?> downloadCertificate(@PathVariable Long id) {
        try {
            Certificate certificate = certificateService.findCertificateById(id);
            String type = certificate.getType();

            File pdfFile = pdfGenerationService.generateCertificatePDF(id);

            InputStreamResource resource = new InputStreamResource(new FileInputStream(pdfFile));
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + pdfFile.getName());

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(pdfFile.length())
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(resource);

        } catch (FileNotFoundException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("PDF file not found.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while downloading the certificate.");
        }
    }



}
