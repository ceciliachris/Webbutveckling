package com.example.demo.folder;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class FolderModelAssembler implements RepresentationModelAssembler<FolderEntity, FolderModel> {

    @Override
    public FolderModel toModel(FolderEntity entity) {
        FolderModel folderModel = new FolderModel(entity);

        folderModel.add(linkTo(methodOn(FolderController.class).getFolderById(entity.getId())).withSelfRel());

        folderModel.add(linkTo(methodOn(FolderController.class).getUserFolders(null)).withRel("all-folders"));

        return folderModel;
    }

    @Override
    public CollectionModel<FolderModel> toCollectionModel(Iterable<? extends FolderEntity> entities) {
        CollectionModel<FolderModel> folderModels = RepresentationModelAssembler.super.toCollectionModel(entities);

        folderModels.add(linkTo(methodOn(FolderController.class).getUserFolders(null)).withSelfRel());

        return folderModels;
    }
}
