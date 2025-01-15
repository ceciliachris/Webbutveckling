package com.example.demo.controllers;

import com.example.demo.models.Folders;
import com.example.demo.repositories.FolderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/folders")
public class FolderController {

    private final FolderRepository folderRepository;

    @Autowired
    public FolderController(FolderRepository folderRepository) {
        this.folderRepository = folderRepository;
    }

    @PostMapping
    public Folders createFolder(@RequestParam String folderName) {
        Folders folders = new Folders();
        folders.setFolderName(folderName);
        return folderRepository.save(folders);
    }

    @GetMapping("/{folderId}")
    public Optional<Folders> getFolderNyId(@PathVariable Long folderId) {
        return folderRepository.findById(folderId);
    }
}
