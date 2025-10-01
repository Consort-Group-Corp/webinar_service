package uz.consortgroup.webinar_service.service.webinar;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import uz.consortgroup.core.api.v1.dto.user.enumeration.UserRole;
import uz.consortgroup.core.api.v1.dto.user.response.UserShortInfoResponseDto;
import uz.consortgroup.core.api.v1.dto.webinar.request.WebinarCreateRequestDto;
import uz.consortgroup.core.api.v1.dto.webinar.request.WebinarUpdateRequestDto;
import uz.consortgroup.core.api.v1.dto.webinar.response.WebinarListItemResponseDto;
import uz.consortgroup.core.api.v1.dto.webinar.response.WebinarListPageResponse;
import uz.consortgroup.core.api.v1.dto.webinar.response.WebinarResponseDto;
import uz.consortgroup.webinar_service.client.UserClient;
import uz.consortgroup.webinar_service.entity.Webinar;
import uz.consortgroup.webinar_service.entity.WebinarParticipant;
import uz.consortgroup.webinar_service.exception.WebinarNotFoundException;
import uz.consortgroup.webinar_service.mapper.WebinarMapper;
import uz.consortgroup.webinar_service.repository.WebinarRepository;
import uz.consortgroup.webinar_service.security.AuthContext;
import uz.consortgroup.webinar_service.service.storage.FileStorageService;
import uz.consortgroup.webinar_service.service.strategy.WebinarCategoryStrategy;
import uz.consortgroup.webinar_service.service.strategy.WebinarCategoryStrategyFactory;
import uz.consortgroup.webinar_service.validator.CourseValidationServiceImpl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
public class WebinarServiceImpl implements WebinarService {

    private final WebinarRepository webinarRepository;
    private final WebinarMapper webinarMapper;
    private final FileStorageService fileStorageService;
    private final AuthContext authContext;
    private final WebinarParticipantService webinarParticipantService;
    private final CourseValidationServiceImpl courseValidationService;
    private final MessageSource messageSource;
    private final UserClient userClient;
    private final WebinarCategoryStrategyFactory webinarCategoryStrategyFactory;

    @Value("${app.preview.base-url}")
    private String previewBaseUrl;

    @Override
    @Transactional
    public WebinarResponseDto createWebinar(WebinarCreateRequestDto dto, MultipartFile file) {
        log.info("Starting webinar creation: {}", dto.getTitle());

        courseValidationService.validateCourseExists(dto.getCourseId());
        UUID currentUserId = authContext.getCurrentUserId();
        UserRole currentRole = authContext.getCurrentUserRole();
        courseValidationService.assertCourseVisibleForUser(dto.getCourseId(), currentUserId, currentRole);

        String previewFilename = null, previewUrl = null;
        if (file != null && !file.isEmpty()) {
            previewFilename = fileStorageService.store(file);
            previewUrl = previewBaseUrl + previewFilename;
        }

        Webinar webinar = buildWebinar(dto, previewFilename, previewUrl);
        webinar.setOnlyCourseParticipants(Boolean.TRUE.equals(dto.getOnlyCourseParticipants()));
        webinar = webinarRepository.save(webinar);


        List<String> identifiers = dto.getParticipants() == null ? List.of() : new ArrayList<>(dto.getParticipants());
        if (!identifiers.isEmpty()) {
            List<UUID> addedUserIds = webinarParticipantService.addParticipants(webinar, identifiers);

            if (Boolean.TRUE.equals(dto.getOnlyCourseParticipants()) && !addedUserIds.isEmpty()) {
                Set<UUID> enrolled = courseValidationService.filterEnrolled(dto.getCourseId(), Set.copyOf(addedUserIds));
                if (enrolled.size() != addedUserIds.size()) {
                    throw new IllegalArgumentException("Some participants are not enrolled in the selected course");
                }
            }
        }

        webinar.setParticipants(webinarParticipantService.getParticipantsByWebinarId(webinar.getId()));
        return webinarMapper.toDto(webinar);
    }

    @Override
    @Transactional
    public WebinarResponseDto updateWebinar(WebinarUpdateRequestDto dto, MultipartFile file) {
        log.info("Starting webinar update: ID={}", dto.getId());

        Webinar webinar = webinarRepository.findById(dto.getId())
                .orElseThrow(() -> new WebinarNotFoundException("Webinar not found with id: " + dto.getId()));

        courseValidationService.validateCourseExists(dto.getCourseId());
        UUID currentUserId = authContext.getCurrentUserId();
        UserRole currentRole = authContext.getCurrentUserRole();
        courseValidationService.assertCourseVisibleForUser(dto.getCourseId(), currentUserId, currentRole);

        String newFilename = webinar.getPreviewFilename();
        String newUrl = webinar.getPreviewUrl();
        if (file != null && !file.isEmpty()) {
            if (webinar.getPreviewFilename() != null) fileStorageService.delete(webinar.getPreviewFilename());
            newFilename = fileStorageService.store(file);
            newUrl = previewBaseUrl + newFilename;
        }

        updateWebinarFields(dto, webinar, newFilename, newUrl);
        webinar.setOnlyCourseParticipants(Boolean.TRUE.equals(dto.getOnlyCourseParticipants()));
        webinarRepository.save(webinar);


        if (dto.getParticipants() != null) {
            List<WebinarParticipant> newParts = webinarParticipantService.updateParticipants(webinar, new ArrayList<>(dto.getParticipants()));

            if (Boolean.TRUE.equals(dto.getOnlyCourseParticipants()) && !newParts.isEmpty()) {
                Set<UUID> ids = newParts.stream().map(WebinarParticipant::getUserId).collect(Collectors.toSet());
                Set<UUID> enrolled = courseValidationService.filterEnrolled(dto.getCourseId(), ids);
                if (enrolled.size() != ids.size()) {
                    throw new IllegalArgumentException("Some participants are not enrolled in the selected course");
                }
            }

            webinar.getParticipants().clear();
            webinar.getParticipants().addAll(webinarParticipantService.getParticipantsByWebinarId(webinar.getId()));
        }

        return webinarMapper.toDto(webinar);
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

    @Override
    public WebinarListPageResponse getWebinars(String category, String lang, Pageable pageable) {
        UUID userId = authContext.getCurrentUserId();
        UserRole role = authContext.getCurrentUserRole();

        WebinarCategoryStrategy strategy = webinarCategoryStrategyFactory.getStrategy(category);

        Specification<Webinar> spec = strategy.getSpecification(userId, role);

        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                strategy.getSort()
        );

        Page<Webinar> page = webinarRepository.findAll(spec, sortedPageable);

        Map<UUID, UserShortInfoResponseDto> userMap = Collections.emptyMap();
        if (!page.isEmpty()) {
            Set<UUID> tutorIds = page.stream()
                    .map(Webinar::getCreatedBy)
                    .collect(Collectors.toSet());

            userMap = userClient.getShortInfoBulk(new ArrayList<>(tutorIds));

    }

        final Map<UUID, UserShortInfoResponseDto> finalUserMap = userMap;

        List<WebinarListItemResponseDto> webinars = page.getContent().stream()
                .map(webinar -> {
                    UserShortInfoResponseDto tutor = finalUserMap.get(webinar.getCreatedBy());
                    return WebinarListItemResponseDto.builder()
                            .id(webinar.getId())
                            .title(webinar.getTitle())
                            .startTime(webinar.getStartTime())
                            .endTime(webinar.getEndTime())
                            .platformUrl(webinar.getPlatformUrl())
                            .previewUrl(webinar.getPreviewFilename())
                            .tutors(List.of(tutor))
                            .build();
                }).toList();

        String message = page.isEmpty()
                ? messageSource.getMessage("webinar.empty", null, Locale.forLanguageTag(lang))
                : null;

        return WebinarListPageResponse.builder()
                .webinars(webinars)
                .empty(webinars.isEmpty())
                .message(message)
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .build();
    }

    private void updateWebinarFields(WebinarUpdateRequestDto dto, Webinar webinar, String newFilename, String newUrl) {
        webinar.setTitle(dto.getTitle());
        webinar.setCategory(dto.getCategory());
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
                .category(dto.getCategory())
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
