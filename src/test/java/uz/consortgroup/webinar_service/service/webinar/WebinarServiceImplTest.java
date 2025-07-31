package uz.consortgroup.webinar_service.service.webinar;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import uz.consortgroup.core.api.v1.dto.webinar.enumeration.LanguageCode;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WebinarServiceImplTest {

    @Mock
    private WebinarRepository webinarRepository;

    @Mock
    private WebinarMapper webinarMapper;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private AuthContext authContext;

    @Mock
    private WebinarParticipantService webinarParticipantService;

    @Mock
    private CourseValidationService courseValidationService;

    @Mock
    private MultipartFile file;

    @InjectMocks
    private WebinarServiceImpl service;

    @Test
    void createWebinar_shouldSucceed_withFileAndParticipants() {
        UUID courseId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        WebinarCreateRequestDto dto = WebinarCreateRequestDto.builder()
                .title("Test Webinar")
                .courseId(courseId)
                .participants(List.of("email@example.com"))
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusHours(1))
                .languageCode(LanguageCode.RU)
                .platformUrl("http://zoom.com")
                .build();

        when(file.isEmpty()).thenReturn(false);
        when(fileStorageService.store(file)).thenReturn("file.png");
        when(authContext.getCurrentUserId()).thenReturn(userId);
        when(webinarRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(webinarParticipantService.addParticipants(any(), any())).thenReturn(List.of(UUID.randomUUID()));
        when(webinarParticipantService.getParticipantsByWebinarId(any())).thenReturn(List.of());
        when(webinarMapper.toDto(any())).thenReturn(new WebinarResponseDto());

        WebinarResponseDto response = service.createWebinar(dto, file);

        assertNotNull(response);
        verify(fileStorageService).store(file);
        verify(webinarRepository).save(any());
    }

    @Test
    void updateWebinar_shouldSucceed_withFileAndParticipants() {
        UUID webinarId = UUID.randomUUID();
        Webinar webinar = new Webinar();
        webinar.setId(webinarId);
        webinar.setPreviewFilename("old.png");
        webinar.setParticipants(new ArrayList<>());

        WebinarUpdateRequestDto dto = WebinarUpdateRequestDto.builder()
                .id(webinarId)
                .title("Updated")
                .courseId(UUID.randomUUID())
                .participants(List.of("email"))
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusHours(1))
                .languageCode(LanguageCode.RU)
                .platformUrl("http://test.com")
                .build();

        when(webinarRepository.findById(webinarId)).thenReturn(Optional.of(webinar));
        when(file.isEmpty()).thenReturn(false);
        when(fileStorageService.store(file)).thenReturn("new.png");
        when(webinarParticipantService.updateParticipants(any(), any())).thenReturn(List.of());
        when(webinarParticipantService.getParticipantsByWebinarId(any())).thenReturn(List.of());
        when(webinarMapper.toDto(any())).thenReturn(new WebinarResponseDto());

        WebinarResponseDto response = service.updateWebinar(dto, file);

        assertNotNull(response);
        verify(fileStorageService).delete("old.png");
        verify(fileStorageService).store(file);
        verify(webinarRepository).save(any());
    }

    @Test
    void updateWebinar_shouldThrow_whenWebinarNotFound() {
        UUID webinarId = UUID.randomUUID();
        WebinarUpdateRequestDto dto = WebinarUpdateRequestDto.builder()
                .id(webinarId)
                .courseId(UUID.randomUUID())
                .title("Nope")
                .build();

        when(webinarRepository.findById(webinarId)).thenReturn(Optional.empty());

        assertThrows(WebinarNotFoundException.class, () -> service.updateWebinar(dto, file));
    }

    @Test
    void deleteWebinar_shouldSucceed_withPreviewAndParticipants() {
        UUID webinarId = UUID.randomUUID();
        Webinar webinar = new Webinar();
        webinar.setId(webinarId);
        webinar.setPreviewFilename("preview.jpg");
        webinar.setParticipants(new ArrayList<>(List.of(new WebinarParticipant())));

        when(webinarRepository.findById(webinarId)).thenReturn(Optional.of(webinar));

        service.deleteWebinar(webinarId);

        verify(fileStorageService).delete("preview.jpg");
        verify(webinarRepository).delete(webinar);
    }

    @Test
    void deleteWebinar_shouldThrow_whenWebinarNotFound() {
        UUID id = UUID.randomUUID();
        when(webinarRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(WebinarNotFoundException.class, () -> service.deleteWebinar(id));
    }
}
