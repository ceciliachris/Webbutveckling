package com.example.demo.folder;

import com.example.demo.user.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service-klass som skapar, hämtar och hanterar mappar i databasen.
 */

@Service
public class FolderService {

    private final FolderRepository folderRepository;

    /**
     * Konstruktor som injicerar FolderRepository.
     * @param folderRepository Repository för att hantera databasanrop för mappar.
     */
    @Autowired
    public FolderService(FolderRepository folderRepository) {
        this.folderRepository = folderRepository;
    }

    /**
     * Skapar en ny mapp för en given användare.
     * @param folderName Namnet på den nya mappen.
     * @param user Den autentiserade användaren som äger mappen.
     * @return Den skapade och sparade FolderEntity.
     * @throws RuntimeException Om användaren inte är autentiserad.
     */
    public FolderEntity createFolder(String folderName, UserEntity user) {
        if(user == null) {
            throw new RuntimeException("User is not authenticated.");
        }

        FolderEntity folder = new FolderEntity();
        folder.setFolderName(folderName);
        folder.setUser(user);
        return folderRepository.save(folder);
    }

    /**
     * Hämtar en mapp baserat på dess ID.
     * @param folderId ID för den sökta mappen.
     * @return En Optional med FolderEntity om den hittas, annars en tom Optional.
     */
    public Optional<FolderEntity> getFolderById(Long folderId) {
        return folderRepository.findById(folderId);
    }

    /**
     * Hämtar alla mappar som tillhör en viss användare.
     * @param user Användaren vars mappar ska hämtas.
     * @return En lista med alla mappar som tillhör användaren.
     */
    public List<FolderEntity> getUserFolder(UserEntity user) {
        return folderRepository.findAllByUser(user);
    }
}
