package com.example.demo.controllers;

import com.example.demo.models.FileEntity;
import com.example.demo.models.User;
import com.example.demo.services.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileService fileService;

    @Autowired
    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/upload/{folderId}")
    public ResponseEntity<String> uploadFile(@PathVariable Long folderId, @RequestParam("file") MultipartFile file, @AuthenticationPrincipal User user) {

        try {
            System.out.println("Folder ID: " + folderId);
            System.out.println("File name: " + file.getOriginalFilename());
            System.out.println("User: " + user.getName());

            fileService.uploadFile(folderId, file, user);
            return ResponseEntity.ok("File uploaded successfully");
        } catch (IllegalArgumentException e) {
            System.err.println("IllegalArgumentException: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("HÄr är det fel");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload file");
        }
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<String> deleteFile(@PathVariable Long fileId, @AuthenticationPrincipal User user) {
        try {
            fileService.deleteFile(fileId, user);
            return ResponseEntity.ok("File deleted successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have permission to delete this file");
        }
        
    }

    @GetMapping("/{fileId}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable Long fileId, @AuthenticationPrincipal User user) {
        try {
            System.out.println("Requesting download for file ID: " + fileId);
            FileEntity fileEntity = fileService.downloadFile(fileId, user)
                    .orElseThrow(() -> new IllegalArgumentException("File not found"));

            String mimeType = fileEntity.getFileType();
            if (mimeType == null || !mimeType.contains("/")) {
                mimeType = "application/octet-stream";
            }

            System.out.println("File Type in DB: " + fileEntity.getFileType());

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileEntity.getFileName() + "\"");
            headers.add(HttpHeaders.CONTENT_TYPE, mimeType);

            return new ResponseEntity<>(fileEntity.getData(), headers, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("permission")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
            }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
