package uz.consortgroup.webinar_service.service.webinar;

import org.springframework.web.multipart.MultipartFile;
import uz.consortgroup.core.api.v1.dto.webinar.request.WebinarCreateRequestDto;
import uz.consortgroup.core.api.v1.dto.webinar.request.WebinarUpdateRequestDto;
import uz.consortgroup.core.api.v1.dto.webinar.response.WebinarResponseDto;

import java.util.UUID;

public interface WebinarService {
    WebinarResponseDto createWebinar(WebinarCreateRequestDto dto, MultipartFile file);
    WebinarResponseDto updateWebinar(WebinarUpdateRequestDto dto, MultipartFile file);
    void deleteWebinar(UUID webinarId);
}
