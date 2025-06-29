package com.example.ai.edu.springboot.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.ai.edu.springboot.backend.service.SupabaseStorageService;

import java.util.List;

@RestController
@RequestMapping("/api/files")
public class FileDownloadController {

	@Autowired
    private final SupabaseStorageService storageService;

    public FileDownloadController(SupabaseStorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping
    public ResponseEntity<List<String>> listFiles() {
        try {
            return ResponseEntity.ok(storageService.listFiles());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{fileName}")
    public ResponseEntity<InputStreamResource> downloadFile(@PathVariable String fileName) {
        try {
            return storageService.downloadFile(fileName);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
