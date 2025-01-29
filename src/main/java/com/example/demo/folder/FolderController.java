package com.example.demo.folder;

import com.example.demo.user.UserEntity;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/folders")
public class FolderController {

    private final FolderService folderService;

    @Autowired
    public FolderController(FolderService folderService) {
        this.folderService = folderService;
    }

    @PostMapping
    public FolderEntity createFolder(@RequestBody FolderDTO folderDTO, @AuthenticationPrincipal UserEntity user) {
        return folderService.createFolder(folderDTO.getFolderName(), user);
    }

    @GetMapping("/{folderId}")
    public Optional<FolderEntity> getFolderById(@PathVariable Long folderId) {
        return folderService.getFolderById(folderId);
    }

    @GetMapping
    public List<FolderEntity> getUserFolders(@AuthenticationPrincipal UserEntity user) {
        return folderService.getUserFolder(user);
    }

    @Setter
    @Getter
    public static class FolderDTO {
        private String folderName;
    }
}
