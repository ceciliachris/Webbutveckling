package com.example.demo.models;

import jakarta.persistence.*;

@Entity
public class FileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "bytea")
    private byte[] data;

    private String fileName;
    private String fileType;

    @ManyToOne
    @JoinColumn(name = "folder_id")
    private Folders folder;

    public FileEntity() {
    }


    public FileEntity(String fileName, String fileType, byte[] data, Folders folder) {
        this.fileName = fileName;
        this.fileType = fileType;
        this.data = data;
        this.folder = folder;
    }

    public Long getId() {
        return id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public Folders getFolder() {
        return folder;
    }

    public void setFolder(Folders folder) {
        this.folder = folder;
    }
}
