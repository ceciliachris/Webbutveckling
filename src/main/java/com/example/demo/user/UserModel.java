package com.example.demo.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import java.util.UUID;

@Relation(collectionRelation = "users", itemRelation = "user")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserModel extends RepresentationModel<UserModel> {
    private UUID id;
    private String name;
    private String provider;

    public UserModel() {}

    public UserModel(UserEntity user) {
        this.id = user.getId();
        this.name = user.getName();
        this.provider = user.getProvider();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id =id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }
}
