package com.example.demo.repositories;

import com.example.demo.models.Folders;
import com.example.demo.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FolderRepository extends JpaRepository<Folders, Long> {
    List<Folders> findAllByUser(User user);
}
