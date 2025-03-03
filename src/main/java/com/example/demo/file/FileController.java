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

            Map<String, String> response = new HashMap<>();
            response.put("message", "File uploaded successfully");

            EntityModel<Map<String, String>> responseModel = EntityModel.of(response);

            responseModel.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(FileController.class)
                    .getFilesInFolder(folderId, user)).withRel("viewFilesInFolder"));

            return ResponseEntity.ok(responseModel);
        } catch (IllegalArgumentException e) {
            log.warn("Upload failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(EntityModel.of("Invalid request: "+ e.getMessage()));
        } catch (IOException e) {
            log.error("File upload error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(EntityModel.of("Failed to upload file"));
        }
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<?> deleteFile(@PathVariable Long fileId, @AuthenticationPrincipal UserEntity user) {
        try {
            log.info("User {} attempting to delete file with ID {}", user.getId(), fileId);

            Long folderId = fileService.getFileFolder(fileId);

            fileService.deleteFile(fileId, user);

            Map<String, String> response = new HashMap<>();
            response.put("message", "File deleted successfully");

            EntityModel<Map<String, String>> responseModel = EntityModel.of(response);

            if (folderId != null) {
                responseModel.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(FileController.class)
                        .getFilesInFolder(folderId, user)).withRel("viewFilesInFolder"));
            }

            return ResponseEntity.ok(responseModel);
        } catch (ForbiddenException e) {
            log.warn("User {} is not authorized to delete file {}", user.getId(), fileId);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "You do not have permission to delete this file");
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(EntityModel.of(errorResponse));
        } catch (IllegalArgumentException e) {
            log.error("File deletion error: {}", e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "File not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(EntityModel.of(errorResponse));
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
    public ResponseEntity<CollectionModel<FileInfoDTO>> getFilesInFolder(
            @PathVariable Long folderId,
            @AuthenticationPrincipal UserEntity user) {
        try {
            log.info("User {} requesting files in folder {}", user.getId(), folderId);
            List<FileInfoDTO> files = fileService.getFilesInFolder(folderId, user);

            CollectionModel<FileInfoDTO> fileCollection = CollectionModel.of(files);

            fileCollection.add(WebMvcLinkBuilder.linkTo(FileController.class).withRel("files"));

            fileCollection.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(FileController.class)
                            .uploadFile(folderId, null, user)).withRel("upload")
                    .withTitle("Upload a new file to this folder"));


            return ResponseEntity.ok(fileCollection);
        } catch (ForbiddenException e) {
            log.warn("User {} does not have permission to access folder {}", user.getId(), folderId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        } catch (IllegalArgumentException e) {
            log.error("Folder not found: {}", folderId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
}
