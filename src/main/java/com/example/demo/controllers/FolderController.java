package com.example.demo.controllers;

import com.example.demo.models.Folders;
import com.example.demo.models.User;
import com.example.demo.repositories.FolderRepository;
import com.example.demo.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/folders")
public class FolderController {

    private final FolderRepository folderRepository;
    private final UserRepository userRepository;

    @Autowired
    public FolderController(FolderRepository folderRepository, UserRepository userRepository) {
        this.folderRepository = folderRepository;
        this.userRepository = userRepository;
    }

    @PostMapping
    public Folders createFolder(@RequestBody FolderDTO folderDTO, @AuthenticationPrincipal User user) {
        if (user == null) {
            throw new RuntimeException("User is not authenticated.");
        }

        Folders folders = new Folders();
        folders.setFolderName(folderDTO.getFolderName());
        folders.setUser(user);
        return folderRepository.save(folders);
    }

    @GetMapping("/{folderId}")
    public Optional<Folders> getFolderById(@PathVariable Long folderId) {
        return folderRepository.findById(folderId);
    }

    @GetMapping
    public List<Folders> getUserFolders(@AuthenticationPrincipal User user) {
        return folderRepository.findAllByUser(user);

    }

    public static class FolderDTO {
        private String foldername;

        public String getFolderName() {
            return foldername;
        }

        public void setFolderName(String foldername) {
            this.foldername = foldername;
        }
    }

}
