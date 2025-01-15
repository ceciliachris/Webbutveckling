package com.example.demo.services;

import com.example.demo.models.FileEntity;
import com.example.demo.models.Folders;
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

    public FileEntity uploadFile(Long folderId, MultipartFile file) throws IOException {
        Optional<Folders> folderOptional = folderRepository.findById(folderId);

        if (folderOptional.isEmpty()) {
            throw new IllegalArgumentException("Folder not found");
        }

        Folders folders = folderOptional.get();
        FileEntity fileEntity = new FileEntity(file.getOriginalFilename(), file.getContentType(), file.getBytes(), folders);
        return fileRepository.save(fileEntity);
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
