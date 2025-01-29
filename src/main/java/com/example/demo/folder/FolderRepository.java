package com.example.demo.folder;

import com.example.demo.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FolderRepository extends JpaRepository<FolderEntity, Long> {
    List<FolderEntity> findAllByUser(UserEntity user);
}
