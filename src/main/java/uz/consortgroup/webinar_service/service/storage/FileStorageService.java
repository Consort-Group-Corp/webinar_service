package uz.consortgroup.webinar_service.service.storage;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    String store(MultipartFile file);
    void delete(String fileUrl);
}
