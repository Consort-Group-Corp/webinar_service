package uz.consortgroup.webinar_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uz.consortgroup.core.api.v1.dto.webinar.enumeration.LanguageCode;
import uz.consortgroup.core.api.v1.dto.webinar.request.WebinarCreateRequestDto;
import uz.consortgroup.core.api.v1.dto.webinar.request.WebinarUpdateRequestDto;
import uz.consortgroup.core.api.v1.dto.webinar.response.WebinarResponseDto;
import uz.consortgroup.webinar_service.service.webinar.WebinarService;
import uz.consortgroup.webinar_service.util.AuthTokenFilter;
import uz.consortgroup.webinar_service.util.JwtTokenProvider;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = WebinarController.class)
@AutoConfigureMockMvc(addFilters = false)
class WebinarControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WebinarService webinarService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private AuthTokenFilter authTokenFilter;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createWebinar_shouldReturn201_whenValid() throws Exception {
        WebinarCreateRequestDto dto = new WebinarCreateRequestDto();
        dto.setTitle("Test Webinar");
        dto.setStartTime(LocalDateTime.now());
        dto.setEndTime(LocalDateTime.now().plusHours(1));
        dto.setPlatformUrl("https://zoom.us/test");
        dto.setCourseId(UUID.randomUUID());
        dto.setLanguageCode(LanguageCode.RU);
        dto.setParticipants(List.of(UUID.randomUUID().toString()));

        WebinarResponseDto response = new WebinarResponseDto();
        response.setId(UUID.randomUUID());
        response.setTitle(dto.getTitle());

        Mockito.when(webinarService.createWebinar(any(WebinarCreateRequestDto.class), any()))
                .thenReturn(response);

        MockMultipartFile metadata = new MockMultipartFile(
                "metadata", "", "application/json",
                objectMapper.writeValueAsString(dto).getBytes(StandardCharsets.UTF_8)
        );

        MockMultipartFile file = new MockMultipartFile(
                "file", "preview.png", "image/png", "image-bytes".getBytes()
        );

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/webinars")
                        .file(metadata)
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value(dto.getTitle()));
    }

    @Test
    void updateWebinar_shouldReturn200_whenValid() throws Exception {
        WebinarUpdateRequestDto dto = new WebinarUpdateRequestDto();
        dto.setId(UUID.randomUUID());
        dto.setTitle("Updated Webinar");
        dto.setStartTime(LocalDateTime.now());
        dto.setEndTime(LocalDateTime.now().plusHours(1));
        dto.setPlatformUrl("https://zoom.us/updated");
        dto.setCourseId(UUID.randomUUID());
        dto.setLanguageCode(LanguageCode.RU);
        dto.setParticipants(List.of(UUID.randomUUID().toString()));

        WebinarResponseDto response = new WebinarResponseDto();
        response.setId(dto.getId());
        response.setTitle(dto.getTitle());

        Mockito.when(webinarService.updateWebinar(any(WebinarUpdateRequestDto.class), any()))
                .thenReturn(response);

        MockMultipartFile metadata = new MockMultipartFile(
                "metadata", "", "application/json",
                objectMapper.writeValueAsString(dto).getBytes(StandardCharsets.UTF_8)
        );

        MockMultipartFile file = new MockMultipartFile(
                "file", "updated.png", "image/png", "image-updated".getBytes()
        );

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/webinars")
                        .file(metadata)
                        .file(file)
                        .with(req -> { req.setMethod("PUT"); return req; })
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(dto.getTitle()));
    }

    @Test
    void deleteWebinar_shouldReturn200_whenFound() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/webinars/{webinarId}", id))
                .andExpect(status().isOk());

        Mockito.verify(webinarService).deleteWebinar(eq(id));
    }

    @Test
    void createWebinar_shouldReturn400_whenMetadataInvalid() throws Exception {
        MockMultipartFile metadata = new MockMultipartFile(
                "metadata", "", "application/json", "invalid-json".getBytes()
        );

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/webinars")
                        .file(metadata)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateWebinar_shouldReturn400_whenMetadataInvalid() throws Exception {
        MockMultipartFile metadata = new MockMultipartFile(
                "metadata", "", "application/json", "invalid-json".getBytes()
        );

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/webinars")
                        .file(metadata)
                        .with(req -> { req.setMethod("PUT"); return req; })
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());
    }
}
