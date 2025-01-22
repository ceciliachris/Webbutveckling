package com.example.demo.controllers;

import com.example.demo.models.User;
import com.example.demo.services.FileService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileService fileService;

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
    public ResponseEntity<String> deleteFile(@PathVariable Long fileId) {
        fileService.deleteFile(fileId);
        return ResponseEntity.ok("File deleted successfully");
    }

    @GetMapping("/{fileId}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable Long fileId) {
        return fileService.downloadFile(fileId).map(file -> {
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFileName() + "\"");
            headers.add(HttpHeaders.CONTENT_TYPE, file.getFileType());
            return new ResponseEntity<>(file.getData(), headers, HttpStatus.OK);
        }).orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
}
