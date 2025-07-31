package uz.consortgroup.webinar_service.service.webinar;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import uz.consortgroup.core.api.v1.dto.webinar.request.WebinarCreateRequestDto;
import uz.consortgroup.core.api.v1.dto.webinar.request.WebinarUpdateRequestDto;
import uz.consortgroup.core.api.v1.dto.webinar.response.WebinarResponseDto;
import uz.consortgroup.webinar_service.entity.Webinar;
import uz.consortgroup.webinar_service.entity.WebinarParticipant;
import uz.consortgroup.webinar_service.exception.WebinarNotFoundException;
import uz.consortgroup.webinar_service.mapper.WebinarMapper;
import uz.consortgroup.webinar_service.repository.WebinarRepository;
import uz.consortgroup.webinar_service.security.AuthContext;
import uz.consortgroup.webinar_service.service.storage.FileStorageService;
import uz.consortgroup.webinar_service.validator.CourseValidationService;

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
    private final CourseValidationService courseValidationService;

    @Value("${app.preview.base-url}")
    private String previewBaseUrl;

    @Override
    @Transactional
    public WebinarResponseDto createWebinar(WebinarCreateRequestDto dto, MultipartFile file) {
        log.info("Starting webinar creation: {}", dto.getTitle());
        courseValidationService.validateCourseExists(dto.getCourseId());

        String previewFilename = null;
        String previewUrl = null;

        if (file != null && !file.isEmpty()) {
            previewFilename = fileStorageService.store(file);
            log.info("Got file: name={}, size={}", file.getOriginalFilename(), file.getSize());
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

    @Override
    @Transactional
    public WebinarResponseDto updateWebinar(WebinarUpdateRequestDto dto, MultipartFile file) {
        log.info("Starting webinar update: ID={}", dto.getId());

        Webinar webinar = webinarRepository.findById(dto.getId())
                .orElseThrow(() -> new IllegalArgumentException("Webinar not found with id: " + dto.getId()));

        courseValidationService.validateCourseExists(dto.getCourseId());

        String newFilename = webinar.getPreviewFilename();
        String newUrl = webinar.getPreviewUrl();

        if (file != null && !file.isEmpty()) {
            if (webinar.getPreviewFilename() != null) {
                fileStorageService.delete(webinar.getPreviewFilename());
                log.info("Old preview deleted: {}", webinar.getPreviewFilename());
            }
            newFilename = fileStorageService.store(file);
            newUrl = previewBaseUrl + newFilename;
            log.info("New preview stored: {}", newUrl);
        }

        updateWebinarFields(dto, webinar, newFilename, newUrl);

        webinarRepository.save(webinar);
        log.info("Webinar updated: {}", webinar.getId());

        List<WebinarParticipant> newParticipants = webinarParticipantService.updateParticipants(webinar, dto.getParticipants());
        log.info("Participants updated: {}", newParticipants.size());

        webinar.getParticipants().clear();
        webinar.getParticipants().addAll(webinarParticipantService.getParticipantsByWebinarId(webinar.getId()));

        WebinarResponseDto response = webinarMapper.toDto(webinar);
        log.info("Webinar update completed: ID={}", webinar.getId());
        return response;
    }

    @Override
    @Transactional
    public void deleteWebinar(UUID webinarId) {
        Webinar webinar = webinarRepository.findById(webinarId)
                .orElseThrow(() -> new WebinarNotFoundException("Webinar not found with id: " + webinarId));

        if (webinar.getPreviewFilename() != null) {
            fileStorageService.delete(webinar.getPreviewFilename());
            log.info("Deleted preview: {}", webinar.getPreviewFilename());
        }


        if (!webinar.getParticipants().isEmpty()) {
            webinar.getParticipants().clear();
        }
        log.info("Cleared participants for webinar: {}", webinarId);

        webinarRepository.delete(webinar);
        log.info("Webinar deleted: {}", webinarId);
    }

    private void updateWebinarFields(WebinarUpdateRequestDto dto, Webinar webinar, String newFilename, String newUrl) {
        webinar.setTitle(dto.getTitle());
        webinar.setStartTime(dto.getStartTime());
        webinar.setEndTime(dto.getEndTime());
        webinar.setPlatformUrl(dto.getPlatformUrl());
        webinar.setCourseId(dto.getCourseId());
        webinar.setLanguageCode(dto.getLanguageCode());
        webinar.setPreviewFilename(newFilename);
        webinar.setPreviewUrl(newUrl);
        webinar.setUpdatedAt(LocalDateTime.now());
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
