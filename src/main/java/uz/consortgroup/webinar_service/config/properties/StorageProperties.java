package uz.consortgroup.webinar_service.config.properties;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;

import java.nio.file.Path;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "storage")
@Data
public class StorageProperties {

    @NotNull(message = "Base directory must not be null")
    private Path baseDir;

    @NotNull
    private ImageProperties image;

    @Data
    public static class ImageProperties {

        @NotNull(message = "Subdirectory must not be null")
        private String subDir;

        @NotNull(message = "Max file size must not be null")
        @Positive(message = "Max file size must be positive")
        private DataSize maxFileSize;

        @NotEmpty(message = "Allowed mime types must not be empty")
        private List<String> allowedMimeTypes;

        @NotEmpty(message = "Allowed extensions must not be empty")
        private List<String> allowedExtensions;

        public Path getLocation(Path baseDir) {
            return baseDir.resolve(subDir);
        }
    }
}
