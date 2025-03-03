package com.example.demo.folder;

import com.example.demo.user.UserEntity;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/folders")
public class FolderController {

    private final FolderService folderService;
    private final FolderModelAssembler folderAssembler;

    @Autowired
    public FolderController(FolderService folderService, FolderModelAssembler folderAssembler) {
        this.folderService = folderService;
        this.folderAssembler = folderAssembler;
    }

    @PostMapping
    public ResponseEntity<?> createFolder(@RequestBody FolderDTO folderDTO, @AuthenticationPrincipal UserEntity user) {
        FolderEntity folder = folderService.createFolder(folderDTO.getFolderName(), user);
        FolderModel folderModel = folderAssembler.toModel(folder);

        return ResponseEntity
                .created(folderModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .body(folderModel);
    }

    @GetMapping("/{folderId}")
    public ResponseEntity<FolderModel> getFolderById(@PathVariable Long folderId) {
        return folderService.getFolderById(folderId)
                .map(folderAssembler::toModel)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<CollectionModel<FolderModel>> getUserFolders(@AuthenticationPrincipal UserEntity user) {
        List<FolderEntity> folders = folderService.getUserFolder(user);
        return ResponseEntity.ok(folderAssembler.toCollectionModel(folders));
    }

    @Setter
    @Getter
    public static class FolderDTO {
        private String folderName;
    }
}
