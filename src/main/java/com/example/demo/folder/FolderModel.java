package com.example.demo.folder;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

@Relation(collectionRelation = "folders", itemRelation = "folder")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FolderModel extends RepresentationModel<FolderModel> {

    private Long id;
    private String folderName;

    public FolderModel() {}

    public FolderModel(FolderEntity folder) {
        this.id = folder.getId();
        this.folderName = folder.getFolderName();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }
}
