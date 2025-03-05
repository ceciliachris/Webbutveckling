package com.example.demo.file;

import com.example.demo.exceptions.ForbiddenException;
import com.example.demo.user.UserEntity;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.engine.spi.EntityUniqueKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileService fileService;

    @Autowired
    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/upload/{folderId}")
    public ResponseEntity<?> uploadFile(@PathVariable Long folderId, @RequestParam("file") MultipartFile file, @AuthenticationPrincipal UserEntity user) {
        try {
            log.info("Uploading file '{}' to folder ID: {} by user: {}", file.getOriginalFilename(), folderId, user.getName());

            fileService.uploadFile(folderId, file, user);

            return ResponseEntity.ok(Map.of("message", "File uploaded successfully"));
        } catch (IllegalArgumentException e) {
            log.warn("Upload failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error","Invalid request: "+ e.getMessage()));
        } catch (IOException e) {
            log.error("File upload error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error","Failed to upload file"));
        }
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<?> deleteFile(@PathVariable Long fileId, @AuthenticationPrincipal UserEntity user) {
        try {
            log.info("User {} attempting to delete file with ID {}", user.getId(), fileId);

            Long folderId = fileService.getFileFolder(fileId);
            fileService.deleteFile(fileId, user);

            return ResponseEntity.ok(Map.of("message", "File deleted successfully"));
        } catch (ForbiddenException e) {
            log.warn("User {} is not authorized to delete file {}", user.getId(), fileId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "You do not have permission to delete this file"));
        } catch (IllegalArgumentException e) {
            log.error("File deletion error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "File not found"));
        }
    }

    @GetMapping("/{fileId}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable Long fileId, @AuthenticationPrincipal UserEntity user) {
        try {
            log.info("User {} is requesting download for file ID: {}", user.getId(),fileId);
            FileEntity fileEntity = fileService.downloadFile(fileId, user);

            String mimeType = fileEntity.getFileType();
            if (mimeType == null || !mimeType.contains("/")) {
                mimeType = "application/octet-stream";
            }

            log.info("File Type in DB: {}", fileEntity.getFileType());

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileEntity.getFileName() + "\"");
            headers.add(HttpHeaders.CONTENT_TYPE, mimeType);

            return new ResponseEntity<>(fileEntity.getData(), headers, HttpStatus.OK);
        } catch (ForbiddenException e) {
            log.warn("User {} does not have permission to access file {}", user.getId(), fileId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        } catch (IllegalArgumentException e) {
            log.error("File not found: {}", fileId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            log.error("Unexpected error while downloading file ID: {}", fileId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/folder/{folderId}/files")
    public ResponseEntity<List<FileInfoDTO>> getFilesInFolder(
            @PathVariable Long folderId,
            @AuthenticationPrincipal UserEntity user) {
        try {
            log.info("User {} requesting files in folder {}", user.getId(), folderId);
            List<FileInfoDTO> files = fileService.getFilesInFolder(folderId, user);

            return ResponseEntity.ok(files);
        } catch (ForbiddenException e) {
            log.warn("User {} does not have permission to access folder {}", user.getId(), folderId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        } catch (IllegalArgumentException e) {
            log.error("Folder not found: {}", folderId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
}
