package uz.consortgroup.webinar_service.service.webinar;

import org.springframework.web.multipart.MultipartFile;
import uz.consortgroup.core.api.v1.dto.webinar.request.WebinarCreateRequestDto;
import uz.consortgroup.core.api.v1.dto.webinar.response.WebinarResponseDto;

public interface WebinarService {
    WebinarResponseDto create(WebinarCreateRequestDto dto, MultipartFile file);
}
