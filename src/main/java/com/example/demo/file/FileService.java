package com.example.demo.file;

import com.example.demo.exceptions.ForbiddenException;
import com.example.demo.exceptions.ResourceNotFoundException;
import com.example.demo.folder.FolderEntity;
import com.example.demo.user.UserEntity;
import com.example.demo.folder.FolderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

@Slf4j
@Service
public class FileService {

    private final FileRepository fileRepository;
    private final FolderRepository folderRepository;

    public FileService(FileRepository fileRepository, FolderRepository folderRepository) {
        this.fileRepository = fileRepository;
        this.folderRepository = folderRepository;
    }

    /**
     * Laddar upp en fil till en specifik mapp.
     *
     * @param folderId ID för mappen där filen ska laddas upp.
     * @param file     Filen som laddas upp.
     * @param user     Användaren som förösker ladda upp filen.
     * @throws IOException Om det uppstår problem vid läsning av filen.
     * @throws ForbiddenException Om användaren inte har behörighet att ladda upp filen till mappen.
     */
    public FileEntity uploadFile(Long folderId, MultipartFile file, UserEntity user) throws IOException {
        FolderEntity folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new ResourceNotFoundException("Folder not found with id" + folderId));
        log.info("Uploading file '{}' to folder ID: {} by user: {}", file.getOriginalFilename(), folderId, user.getName());

        if (!folder.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("You do not have permission to upload files to this folder");
        }

        String mimeType = file.getContentType();
        if (mimeType == null || !mimeType.contains("/")) {
            mimeType = "application/octet-stream";
        }

        FileEntity fileEntity = new FileEntity(
                file.getOriginalFilename(),
                file.getContentType(),
                file.getBytes(),
                folder
        );

        fileRepository.save(fileEntity);
        log.info("File '{}' upploaded successfully to folder ID: {}", fileEntity.getFileName(), folderId);

        return fileEntity;
    }

    /**
     * Tar bort en fil om användaren har behörighet.
     *
     * @param fileId ID för filen som ska tas bort.
     * @param user   Användaren som försöker ta bort filen.
     * @throws ResourceNotFoundException Om filen inte hittas.
     * @throws ForbiddenException Om användaren inte har behörighet att ta bort filen.
     */
    public void deleteFile(Long fileId, UserEntity user) {
        FileEntity fileEntity = fileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found with id " + fileId));
        log.info("User {} attempting to delete file with ID: {}", user.getId(), fileId);

        if (!fileEntity.getFolder().getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("You do not have permission to delete this file");
        }

        fileRepository.delete(fileEntity);
        log.info("File with ID: {} deleted successfully", fileId);
    }

    /**
     * Hämtar en fil om användaren har behörighet att ladda ner den.
     *
     * @param fileId Id för filen som ska laddas ner.
     * @param user   Användaren som förösker ladda ner filen.
     * @return FileEntity om användaren har behörighet att ladda ner filen.
     * @throws ResourceNotFoundException Om filen inte hittas.
     * @throws ForbiddenException Om användaren inte har behörighet att ladda ner filen.
     */
    public FileEntity downloadFile(Long fileId, UserEntity user) {
        FileEntity fileEntity = fileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found with id " + fileId));
        log.info("User {} requesting download for file ID: {}", user.getId(), fileId);

        if (!fileEntity.getFolder().getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("You do not have permission to download this file");
        }
        return fileEntity;
    }

    /**
     * Hämtar information om alla filer i en mapp om användaren har behörighet.
     *
     * @param folderId Id på mappen där man vill hämta filernas information.
     * @param user Användaren som förösker hämta filernas information.
     * @return En lista av FileInfoDTO som innehåller information om filerna i den angivna mappen.
     * @throws ResourceNotFoundException Om mappen med det angivet ID inte hittas.
     * @throws ForbiddenException Om användaren inte har behörighet att se innehållet i mappen.
     */
    public List<FileEntity> getFilesInFolder(Long folderId, UserEntity user) {
        FolderEntity folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new ResourceNotFoundException("Folder not found"));

        if (!folder.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("You don't have permission to access this folder");
        }

        return fileRepository.findByFolder(folder);
    }

    /**
     * Hämtar mappen för en given fil baserat på filens ID.
     * Om filen inte kan hittas kastas en exception.
     *
     * @param fileId ID för filen vars mapp ska hämtas.
     * @return ID för mappen som filen tillhör. Om ett fel inträffar returneras null.
     */

    public Long getFileFolder(Long fileId) {
        try {
            FileEntity fileEntity = fileRepository.findById(fileId)
                    .orElseThrow(()-> new ResourceNotFoundException("File not found with id " + fileId));
            return fileEntity.getFolder().getId();
        } catch (Exception e) {
            log.error("Error getting folder for file ID: {}" + fileId, e);
            return null;
        }
    }
}
