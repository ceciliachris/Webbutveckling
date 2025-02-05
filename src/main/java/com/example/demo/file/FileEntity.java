package com.example.demo.file;

import com.example.demo.folder.FolderEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class FileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String fileName;
    private String fileType;

    @Column(columnDefinition = "bytea")
    private byte[] data;
    private LocalDateTime uploadDate;

    @ManyToOne
    @JoinColumn(name = "folder_id")
    private FolderEntity folder;


    public FileEntity(String fileName, String fileType, byte[] data, FolderEntity folder) {
        this.fileName = fileName;
        this.fileType = (fileType != null && fileType.contains("/")) ? fileType : determineContentType(fileName);
        this.data = data;
        this.folder = folder;
        this.uploadDate = LocalDateTime.now();
    }

    private String determineContentType(String fileName) {
        if (fileName == null) return "application/octet-stream";

        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        switch (extension) {
            case "pdf": return "application/pdf";
            case "jpg": return "application/jpeg";
            case "png": return "application/png";
            case "jpeg": return "application/jpeg";
            case "txt": return "application/txt";
            default: return "application/octet-stream";
        }
    }
}
