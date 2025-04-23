package com.project.decentpanch.DTOs;


import lombok.Data;

@Data
public class DeathCertificateDTO {
    private String deceasedName;
    private String dob;
    private String dod;
    private String placeOfDeath;
    private String causeOfDeath;
    private String gender;
}
