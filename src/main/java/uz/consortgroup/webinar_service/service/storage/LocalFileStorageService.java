package uz.consortgroup.webinar_service.service.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import uz.consortgroup.webinar_service.config.properties.StorageProperties;
import uz.consortgroup.webinar_service.exception.FileStorageException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocalFileStorageService implements FileStorageService {

    private final StorageProperties props;

    @Override
    public String store(MultipartFile file) {
        String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String extension = getFileExtension(originalFilename);
        String filename = UUID.randomUUID() + extension;

        Path targetPath = props.getBaseDir().resolve("webinars").resolve(filename);
        try {
            Files.createDirectories(targetPath.getParent());
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            log.info("Stored file to {}", targetPath);
        } catch (IOException e) {
            log.error("Failed to store file", e);
            throw new FileStorageException("Failed to store file", e);
        }

        return filename;
    }

    @Override
    public void delete(String fileUrl) {
        try {
            Path path = props.getBaseDir().resolve("webinars").resolve(fileUrl);
            Files.deleteIfExists(path);
            log.info("Deleted file {}", path);
        } catch (IOException e) {
            log.error("Failed to store file", e);
            throw new FileStorageException("Failed to delete file", e);
        }
    }

    private String getFileExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        return (dot >= 0) ? filename.substring(dot) : "";
    }
}
