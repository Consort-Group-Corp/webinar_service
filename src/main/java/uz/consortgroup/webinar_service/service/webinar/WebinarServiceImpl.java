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
import java.util.UUID;

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
        log.info("Starting webinar creation: {}", dto.getTitle());

        String previewFilename = null;
        String previewUrl = null;
        if (file != null && !file.isEmpty()) {
            previewFilename = fileStorageService.store(file);
            previewUrl = previewBaseUrl + previewFilename;
            log.debug("Preview stored: {}", previewUrl);
        }

        Webinar webinar = buildWebinar(dto, previewFilename, previewUrl);
        webinar = webinarRepository.save(webinar);
        log.info("Webinar saved with ID: {}", webinar.getId());

        List<String> identifiers = dto.getParticipants();
        log.debug("Adding participants: count={}", identifiers.size());
        List<UUID> addedUserIds = webinarParticipantService.addParticipants(webinar, identifiers);
        log.info("Added {} participants", addedUserIds.size());

        webinarRepository.flush();
        log.debug("Database flush completed");

        webinar.setParticipants(webinarParticipantService.getParticipantsByWebinarId(webinar.getId()));
        log.debug("Loaded participants: {}", webinar.getParticipants().size());

        WebinarResponseDto response = webinarMapper.toDto(webinar);
        log.info("Webinar creation completed: ID={}", webinar.getId());
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
