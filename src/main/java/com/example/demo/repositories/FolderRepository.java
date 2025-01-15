package com.example.demo.repositories;

import com.example.demo.models.Folders;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FolderRepository extends JpaRepository<Folders, Long> {
}
