package com.example.demo.file;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class FileInfoDTO {
    private Long id;
    private String fileName;
    private String fileType;
    private long fileSize;
    private LocalDateTime uploadDate;
}