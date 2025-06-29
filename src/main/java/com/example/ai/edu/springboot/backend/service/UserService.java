package com.example.ai.edu.springboot.backend.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.ai.edu.springboot.backend.entity.User;
import com.example.ai.edu.springboot.backend.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public void saveUserFromCSV(MultipartFile file) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");

                if (data.length >= 3) {
                    User user = new User();
                    user.setFirstName(data[0].trim());
                    user.setLastName(data[1].trim());
                    user.setEmail(data[2].trim());

                    userRepository.save(user);
                }
            }
        }
    }
}

