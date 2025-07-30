package uz.consortgroup.webinar_service.service.webinar;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import uz.consortgroup.core.api.v1.dto.webinar.request.WebinarCreateRequestDto;
import uz.consortgroup.core.api.v1.dto.webinar.response.WebinarResponseDto;
import uz.consortgroup.webinar_service.entity.Webinar;
import uz.consortgroup.webinar_service.mapper.WebinarMapper;
import uz.consortgroup.webinar_service.repository.WebinarRepository;
import uz.consortgroup.webinar_service.security.AuthContext;
import uz.consortgroup.webinar_service.service.storage.FileStorageService;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class WebinarServiceImpl implements WebinarService {

    private final WebinarRepository webinarRepository;
    private final WebinarMapper webinarMapper;
    private final FileStorageService fileStorageService;
    private final AuthContext authContext;
    private final WebinarParticipantService webinarParticipantService;


    @Value("${app.preview.base-url}")
    private String previewBaseUrl;

    @Override
    @Transactional
    public WebinarResponseDto create(WebinarCreateRequestDto dto, MultipartFile file) {
        log.info("Creating new webinar with title='{}', startTime={}, endTime={}, participants={}",
                dto.getTitle(), dto.getStartTime(), dto.getEndTime(), dto.getParticipants().size());

        String filename = null;
        String previewUrl = null;

        if (file != null && !file.isEmpty()) {
            filename = fileStorageService.store(file);
            previewUrl = previewBaseUrl + filename;
            log.info("Uploaded preview file '{}', accessible at '{}'", filename, previewUrl);
        }

        Webinar webinar = buildWebinar(dto, filename, previewUrl);
        webinarRepository.save(webinar);
        log.info("Webinar saved with ID={}", webinar.getId());

        List<String> participantIdentifiers = dto.getParticipants();
        webinarParticipantService.addParticipants(webinar, participantIdentifiers);
        log.info("Participants successfully added to webinar ID={}", webinar.getId());

        Webinar updated = webinarRepository.findById(webinar.getId())
                .orElseThrow(() -> new RuntimeException("Webinar not found after participant insertion"));

        WebinarResponseDto response = webinarMapper.toDto(updated);
        log.info("Webinar creation completed. Returning response DTO for ID={}", webinar.getId());

        return response;
    }

    private Webinar buildWebinar(WebinarCreateRequestDto dto, String filename, String previewUrl) {
        return Webinar.builder()
                .title(dto.getTitle())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .platformUrl(dto.getPlatformUrl())
                .courseId(dto.getCourseId())
                .languageCode(dto.getLanguageCode())
                .createdBy(authContext.getCurrentUserId())
                .createdAt(LocalDateTime.now())
                .previewFilename(filename)
                .previewUrl(previewUrl)
                .build();
    }
}

