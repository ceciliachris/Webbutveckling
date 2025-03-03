package com.example.demo.file;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class FileInfoDTO extends RepresentationModel<FileInfoDTO> {
    private Long id;
    private String fileName;
    private String fileType;
    private long fileSize;
    private LocalDateTime uploadDate;
}