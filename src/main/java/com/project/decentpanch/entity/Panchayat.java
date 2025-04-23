package com.project.decentpanch.entity;

import jakarta.persistence.Entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Panchayat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long panchayatId;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "userid")
    private User user;

    private String panchayatName;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] logoImage;

    private String officerName;

    private String address;

    private String contactDetails;
}

