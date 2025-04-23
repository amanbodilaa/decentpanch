package com.project.decentpanch.service;
import com.project.decentpanch.entity.Certificate;
import com.project.decentpanch.exceptions.CertificateNotFoundException;
import com.project.decentpanch.exceptions.InvalidCertificateStatusException;
import com.project.decentpanch.repo.CertificateRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Service
public class CertificateService {
    @Autowired
    CertificateRepository certificateRepository;

    public Certificate addRequest(Certificate certificate) {
        try {
            certificate.setStatus("PENDING");
            certificate.setRequestDate(LocalDateTime.now());
            return certificateRepository.save(certificate);
        } catch (Exception e) {
            throw new RuntimeException("Error saving certificate request", e);
        }
    }

    public List<Certificate> getCertificatesByUserId(Long userId) {
        try {
            List<Certificate> certificates = certificateRepository.findByUser_Userid(userId);
            if (certificates.isEmpty()) {
                throw new RuntimeException("No certificate requests found for user with ID: " + userId);
            }
            return certificates;
        } catch (Exception e) {
            throw new RuntimeException("Error fetching certificates for user ID " + userId, e);
        }
    }

    public List<Certificate> getPendingCertificates() throws Exception {
        try {
            List<Certificate> pendingCertificates = certificateRepository.findByStatus("PENDING");
            if (pendingCertificates.isEmpty()) {
                throw new Exception("No pending certificates found.");
            }
            return pendingCertificates;
        } catch (Exception e) {
            throw new Exception("Error fetching pending certificates: " + e.getMessage(), e);
        }
    }

    @Transactional
    public Certificate approveCertificate(Long id) {
        Optional<Certificate> certificateOptional = certificateRepository.findById(id);
        if (certificateOptional.isEmpty()) {
            throw new CertificateNotFoundException("Certificate with ID " + id + " not found");
        }
        Certificate certificate = certificateOptional.get();
        if (certificate.getStatus().equals("APPROVED") || certificate.getStatus().equals("REJECTED")) {
            throw new InvalidCertificateStatusException("Certificate is already " + certificate.getStatus());
        }
        certificate.setStatus("APPROVED");
        certificate.setIssuedDate(LocalDateTime.now());

        return certificateRepository.save(certificate);
    }

    @Transactional
    public Certificate rejectCertificate(Long id) {
        Optional<Certificate> certificateOptional = certificateRepository.findById(id);
        if (certificateOptional.isEmpty()) {
            throw new CertificateNotFoundException("Certificate with ID " + id + " not found");
        }
        Certificate certificate = certificateOptional.get();

        if ("APPROVED".equalsIgnoreCase(certificate.getStatus()) || "REJECTED".equalsIgnoreCase(certificate.getStatus())) {
            throw new InvalidCertificateStatusException("Certificate is already " + certificate.getStatus());
        }
        certificate.setStatus("REJECTED");
        certificate.setIssuedDate(LocalDateTime.now());
        return certificateRepository.save(certificate);
    }

    public Certificate findCertificateById(Long id) {
        return certificateRepository.findById(id)
                .orElseThrow(() -> new CertificateNotFoundException("Certificate with ID " + id + " not found"));
    }

}
