package com.example.ai.edu.springboot.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;

@Service
public class SupabaseStorageService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.api-key}")
    private String supabaseApiKey;

    @Value("${supabase.bucket}")
    private String bucket;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public List<String> listFiles() throws Exception {
        String listUrl = supabaseUrl + "/storage/v1/object/public/" + bucket;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(listUrl))
                .header("Authorization", "Bearer " + supabaseApiKey)
                .header("Content-Type", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        List<Map<String, Object>> fileList = mapper.readValue(
            response.body(),
            new TypeReference<List<Map<String, Object>>>() {}
        );

        return fileList.stream()
                .map(file -> file.get("name").toString())
                .collect(Collectors.toList());
    }


    public ResponseEntity<InputStreamResource> downloadFile(String fileName) throws Exception {
        String downloadUrl = supabaseUrl + "/storage/v1/object/public/" + bucket + "//" + fileName;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(downloadUrl))
                .header("Authorization", "Bearer " + supabaseApiKey)
                .GET()
                .build();

        HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());

        if (response.statusCode() == 200) {
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(new InputStreamResource(response.body()));
        } else {
            return ResponseEntity.status(response.statusCode())
                    .body(null);
        }
    }
}

