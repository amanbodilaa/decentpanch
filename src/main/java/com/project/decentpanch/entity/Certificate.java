package com.project.decentpanch.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "certificate")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Certificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "type", nullable = false, length = 50)
    private String type;

    @Column(name = "details", columnDefinition = "json", nullable = false)
    private String details;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "applicant_name", nullable = false, length = 255)
    private String applicantName;

    @CreationTimestamp
    @Column(name = "request_date", updatable = false)
    private LocalDateTime requestDate;

    @Column(name = "issued_date")
    private LocalDateTime issuedDate;

    @Column(name = "registration_no", unique = true)
    private String registrationNo;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;  // assuming User entity exists

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "panchayat_id", nullable = false)
    private Panchayat panchayat;

}
