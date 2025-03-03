package com.example.demo.user;

import com.example.demo.folder.FolderController;
import org.apache.catalina.User;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class UserModelAssembler implements RepresentationModelAssembler<UserEntity, UserModel> {

    @Override
    public UserModel toModel(UserEntity entity) {
        UserModel userModel = new UserModel(entity);

        userModel.add(linkTo(methodOn(UserController.class).getUserById(entity.getId())).withSelfRel());

        userModel.add(linkTo(methodOn(FolderController.class).getUserFolders(entity)).withRel("folders"));

        return userModel;
    }

    @Override
    public CollectionModel<UserModel> toCollectionModel(Iterable<? extends UserEntity> entities) {
        CollectionModel<UserModel> userModels = RepresentationModelAssembler.super.toCollectionModel(entities);

        userModels.add(linkTo(methodOn(UserController.class).getAllUsers()).withSelfRel());

        return userModels;
    }
}
