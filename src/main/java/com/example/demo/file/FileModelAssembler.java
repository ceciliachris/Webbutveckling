package com.example.demo.file;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class FileModelAssembler implements RepresentationModelAssembler<FileEntity, EntityModel<FileInfoDTO>> {

    @Override
    public EntityModel<FileInfoDTO> toModel(FileEntity file) {
        FileInfoDTO dto = new FileInfoDTO(
                file.getId(),
                file.getFileName(),
                file.getFileType(),
                file.getData() != null ? file.getData().length : 0,
                file.getUploadDate()
        );

        return EntityModel.of(dto,
                linkTo(methodOn(FileController.class).downloadFile(file.getId(),null)).withRel("download"),
                linkTo(methodOn(FileController.class).deleteFile(file.getId(), null)).withRel("delete")
        );
    }

    @Override
    public CollectionModel<EntityModel<FileInfoDTO>> toCollectionModel(Iterable<? extends FileEntity> files) {
        List<EntityModel<FileInfoDTO>> fileModels = StreamSupport.stream(files.spliterator(),false)
                .map(this::toModel)
                .collect(Collectors.toList());

        return CollectionModel.of(fileModels);
    }
}
