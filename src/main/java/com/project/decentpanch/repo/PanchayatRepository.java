package com.project.decentpanch.repo;

import com.project.decentpanch.entity.Panchayat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PanchayatRepository extends JpaRepository<Panchayat, Long> {
}
