package com.project.decentpanch.repo;

import com.project.decentpanch.entity.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CertificateRepository extends JpaRepository<Certificate, Long> {
    List<Certificate> findByUser_Userid(Long userId);
    List<Certificate> findByStatus(String status);
}
