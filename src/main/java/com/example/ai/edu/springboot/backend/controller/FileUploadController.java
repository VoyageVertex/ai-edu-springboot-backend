package com.example.ai.edu.springboot.backend.controller;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import com.opencsv.CSVReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.net.URI;
import java.net.http.*;
import java.net.http.HttpRequest.BodyPublishers;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.MediaType;


@RestController
@RequestMapping("/api/files")
public class FileUploadController {

	@Value("${supabase.url}")
	private String supabaseUrl;

	@Value("${supabase.bucket}")
	private String bucket;

	@Value("${supabase.api-key}")
	private String apiKey;

	private static final HttpClient httpClient = HttpClient.newHttpClient();

	@PostMapping("/upload")
	public ResponseEntity<String> handleFileUpload(@RequestParam("file") MultipartFile file) {
		try {
			String content = new String(file.getBytes(), StandardCharsets.UTF_8);
			// Example: parse file contents
			List<String> lines = Arrays.asList(content.split("\n"));
			lines.forEach(System.out::println); // Replace with actual parsing logic

			return ResponseEntity.ok("File uploaded and parsed successfully.");
		} catch (IOException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("File processing failed: " + e.getMessage());
		}
	}

	@PostMapping("/upload-csv")
	public ResponseEntity<String> handleCsvUpload(@RequestParam("file") MultipartFile file) {
		try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
			CSVReader csvReader = new CSVReader(reader);
			List<String[]> rows = csvReader.readAll();
			for (String[] row : rows) {
				System.out.println(Arrays.toString(row)); // Replace with your logic
			}
			return ResponseEntity.ok("CSV parsed successfully.");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("CSV parsing failed: " + e.getMessage());
		}
	}


	@PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Operation(
	        summary = "Upload a PDF or CSV file",
	        description = "Accepts a single PDF or CSV file for upload",
	        responses = {
	            @ApiResponse(responseCode = "200", description = "File uploaded successfully"),
	            @ApiResponse(responseCode = "400", description = "Invalid file type")
	        }
	    )
	public ResponseEntity<?> uploadFile(@RequestPart("file") MultipartFile file) {
		try {
			// Validate file type
			String contentType = file.getContentType();
			if (!("application/pdf".equals(contentType) || "text/csv".equals(contentType))) {
				return ResponseEntity.badRequest().body("Only PDF and CSV files are allowed.");
			}

			// Generate a random file name to avoid conflicts
			String extension = contentType.equals("application/pdf") ? ".pdf" : ".csv";
			
			String fileName = file.getOriginalFilename(); // use original name
	        if (fileName == null || fileName.isBlank()) {
	            throw new IllegalArgumentException("Invalid file name");
	        }
	        
			//String fileName = UUID.randomUUID() + extension;

			// Construct Supabase upload URL
			String storageUrl = supabaseUrl + "/storage/v1/object/" + bucket + "/" + fileName;

			// Build HTTP PUT request
			HttpRequest request = HttpRequest.newBuilder().uri(URI.create(storageUrl))
					.header("Authorization", "Bearer " + apiKey).header("Content-Type", contentType)
					.header("x-upsert", "true").PUT(BodyPublishers.ofByteArray(file.getBytes())).build();

			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

			if (response.statusCode() == 200 || response.statusCode() == 201) {
				String publicUrl = supabaseUrl + "/storage/v1/object/public/" + bucket + "/" + fileName;
				return ResponseEntity.ok("File uploaded: " + publicUrl);
			} else {
				return ResponseEntity.status(response.statusCode()).body("Upload failed: " + response.body());
			}

		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
		}

	}

}
