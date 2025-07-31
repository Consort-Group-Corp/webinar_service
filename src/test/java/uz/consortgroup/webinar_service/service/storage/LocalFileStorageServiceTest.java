package uz.consortgroup.webinar_service.service.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import uz.consortgroup.webinar_service.config.properties.StorageProperties;
import uz.consortgroup.webinar_service.exception.FileStorageException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class LocalFileStorageServiceTest {

    private LocalFileStorageService storageService;
    private Path baseDir;

    @BeforeEach
    void setUp() throws IOException {
        baseDir = Files.createTempDirectory("webinar-storage-test");
        StorageProperties properties = new StorageProperties();
        properties.setBaseDir(baseDir);
        storageService = new LocalFileStorageService(properties);
    }

    @Test
    void store_shouldSaveFileAndReturnFilename() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file", "example.png", "image/png", "test-data".getBytes()
        );

        String storedFilename = storageService.store(file);

        Path storedFilePath = baseDir.resolve("webinars").resolve(storedFilename);

        assertThat(Files.exists(storedFilePath)).isTrue();
        assertThat(Files.readAllBytes(storedFilePath)).containsExactly("test-data".getBytes());
    }

    @Test
    void delete_shouldRemoveExistingFile() throws IOException {
        Path dir = baseDir.resolve("webinars");
        Files.createDirectories(dir);
        Path filePath = dir.resolve("to-delete.png");
        Files.write(filePath, "delete-me".getBytes());

        assertThat(Files.exists(filePath)).isTrue();

        storageService.delete("to-delete.png");

        assertThat(Files.exists(filePath)).isFalse();
    }

    @Test
    void store_shouldThrow_whenIOExceptionOccurs() {
        MultipartFile brokenFile = new MockMultipartFile("file", (byte[]) null) {
            @Override
            public String getOriginalFilename() {
                return "fail.png";
            }

            @Override
            public java.io.InputStream getInputStream() throws IOException {
                throw new IOException("Simulated error");
            }
        };

        assertThrows(FileStorageException.class, () -> storageService.store(brokenFile));
    }

    @Test
    void delete_shouldThrow_whenIOExceptionOccurs() throws IOException {
        Path dir = baseDir.resolve("webinars");
        Files.createDirectories(dir);
        Path protectedFile = dir.resolve("protected-file.txt");
        Files.write(protectedFile, "data".getBytes());

        protectedFile.toFile().setWritable(false);
        protectedFile.toFile().setReadable(true);
        protectedFile.toFile().setExecutable(false);

        assertThat(Files.exists(protectedFile)).isTrue();

        FileStorageException exception = assertThrows(FileStorageException.class, () ->
                storageService.delete("protected-file.txt")
        );

        assertThat(exception.getMessage()).contains("Failed to delete file");
    }

}
