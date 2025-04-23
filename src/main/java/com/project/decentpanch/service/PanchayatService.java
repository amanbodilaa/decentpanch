package com.project.decentpanch.service;

import com.project.decentpanch.entity.Panchayat;
import com.project.decentpanch.entity.Role;
import com.project.decentpanch.entity.User;
import com.project.decentpanch.repo.PanchayatRepository;
import com.project.decentpanch.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class PanchayatService {

    @Autowired
    private PanchayatRepository panchayatRepository;

    @Autowired
    private UserRepository userRepository;

    public Panchayat getPanchayatById(Long id) {
        return panchayatRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Panchayat not found!"));
    }
    public Panchayat addPanchayatDetails(Panchayat panchayat, MultipartFile logoImage, Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User with ID " + userId + " does not exist!"));

        if (!Role.PANCHAYAT.equals(user.getRoles())) {
            throw new RuntimeException("User with ID " + userId + " is not a Panchayat Officer!");
        }

        try {
            panchayat.setUser(user);
            panchayat.setLogoImage(logoImage.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("Failed to process logo image.", e);
        }

        return panchayatRepository.save(panchayat);
    }



}
