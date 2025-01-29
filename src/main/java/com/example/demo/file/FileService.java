package com.example.demo.file;

import com.example.demo.folder.FolderEntity;
import com.example.demo.user.UserEntity;
import com.example.demo.folder.FolderRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.util.Optional;

@Service
public class FileService {

    private final FileRepository fileRepository;
    private final FolderRepository folderRepository;

    public FileService(FileRepository fileRepository, FolderRepository folderRepository) {
        this.fileRepository = fileRepository;
        this.folderRepository = folderRepository;
    }

    public void uploadFile(Long folderId, MultipartFile file, UserEntity user) throws IOException {
        FolderEntity folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new IllegalArgumentException("Folder not found with id" + folderId));

        System.out.println("Folder owner: " + folder.getUser().getName());
        System.out.println("User: " + user.getName());

        if (!folder.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You do not have permission to upload files to this folder");
        }

        System.out.println("Detected MIME type: " + file.getContentType());

        String mimeType = file.getContentType();
        if (mimeType == null || !mimeType.contains("/")) {
            mimeType = "application/octet-stream";
        }

        FileEntity fileEntity = new FileEntity(
                file.getOriginalFilename(),
                file.getContentType(),
                file.getBytes(),
                folder
        );

        fileRepository.save(fileEntity);
        System.out.println("File uploaded successfully: " + fileEntity.getFileName());

    }

    public void deleteFile(Long fileId, UserEntity user) {
        FileEntity fileEntity = fileRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("File not found with id " + fileId));
        if (!fileEntity.getFolder().getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You do not have permission to delete this file");
        }

        fileRepository.delete(fileEntity);
    }

    public Optional<FileEntity> downloadFile(Long fileId, UserEntity user) {
        FileEntity fileEntity = fileRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("File not found with id " + fileId));
        System.out.println("User ID: " + user.getId());
        System.out.println("File owner ID: " + fileEntity.getFolder().getUser().getId());

        if (!fileEntity.getFolder().getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You do not have permission to download this file");
        }
        return Optional.of(fileEntity);
    }
}