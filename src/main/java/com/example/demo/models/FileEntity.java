package com.example.demo.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Entity
public class FileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "bytea")
    private byte[] data;

    @Setter
    private String fileName;
    private String fileType;

    @ManyToOne
    @JoinColumn(name = "folder_id")
    private Folders folder;

    public FileEntity() {
    }


    public FileEntity(String fileName, String fileType, byte[] data, Folders folder) {
        this.fileName = fileName;
        this.fileType = (fileType != null && fileType.contains("/")) ? fileType : determineContentType(fileName);
        this.data = data;
        this.folder = folder;
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
