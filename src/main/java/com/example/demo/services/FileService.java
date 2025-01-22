package com.example.demo.services;

import com.example.demo.models.FileEntity;
import com.example.demo.models.Folders;
import com.example.demo.models.User;
import com.example.demo.repositories.FileRepository;
import com.example.demo.repositories.FolderRepository;
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

    public void uploadFile(Long folderId, MultipartFile file, User user) throws IOException {
        Folders folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new IllegalArgumentException("Folder not found with id" + folderId));

        System.out.println("Folder owner: " + folder.getUser().getName());
        System.out.println("User: " + user.getName());

        if (!folder.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You do not have permission to upload files to this folder");
        }

        FileEntity fileEntity = new FileEntity(
                file.getOriginalFilename(),
                file.getContentType(),
                file.getBytes(),
                folder
        );

        fileRepository.save(fileEntity);

    }

    public void deleteFile(Long fileId) {
        if (fileRepository.existsById(fileId)) {
            fileRepository.deleteById(fileId);
        } else {
            throw new IllegalArgumentException("File not found with id " + fileId);
        }
    }

    public Optional<FileEntity> downloadFile(Long fileId) {
        return fileRepository.findById(fileId);
    }
}

