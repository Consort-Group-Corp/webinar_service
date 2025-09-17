package uz.consortgroup.webinar_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uz.consortgroup.core.api.v1.dto.webinar.enumeration.LanguageCode;
import uz.consortgroup.core.api.v1.dto.webinar.enumeration.WebinarCategory;
import uz.consortgroup.core.api.v1.dto.webinar.request.WebinarCreateRequestDto;
import uz.consortgroup.core.api.v1.dto.webinar.request.WebinarUpdateRequestDto;
import uz.consortgroup.core.api.v1.dto.webinar.response.WebinarListPageResponse;
import uz.consortgroup.core.api.v1.dto.webinar.response.WebinarResponseDto;
import uz.consortgroup.webinar_service.service.webinar.WebinarService;
import uz.consortgroup.webinar_service.util.AuthTokenFilter;
import uz.consortgroup.webinar_service.util.JwtUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
    private JwtUtils jwtUtils;

    @MockitoBean
    private AuthTokenFilter authTokenFilter;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createWebinar_shouldReturn201_whenValid() throws Exception {
        WebinarCreateRequestDto dto = new WebinarCreateRequestDto();
        dto.setTitle("Test Webinar");
        dto.setCategory(WebinarCategory.PLANNED);
        dto.setStartTime(LocalDateTime.now());
        dto.setEndTime(LocalDateTime.now().plusHours(1));
        dto.setPlatformUrl("https://zoom.us/test");
        dto.setCourseId(UUID.randomUUID());
        dto.setLanguageCode(LanguageCode.RU);
        dto.setParticipants(List.of(UUID.randomUUID().toString()));

        WebinarResponseDto response = new WebinarResponseDto();
        response.setId(UUID.randomUUID());
        response.setTitle(dto.getTitle());

        when(webinarService.createWebinar(any(WebinarCreateRequestDto.class), any()))
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

        when(webinarService.updateWebinar(any(WebinarUpdateRequestDto.class), any()))
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

        verify(webinarService).deleteWebinar(eq(id));
    }


    @Test
    void getWebinars_ShouldReturn200_WithValidParams() throws Exception {
        String category = "planned";
        String lang = "ru";

        WebinarListPageResponse mockResponse = WebinarListPageResponse.builder()
                .empty(true)
                .message("Нет запланированных вебинаров")
                .totalElements(0L)
                .totalPages(0)
                .webinars(Collections.emptyList())
                .build();

        when(webinarService.getWebinars(eq(category), eq(lang), any(Pageable.class)))
                .thenReturn(mockResponse);

        mockMvc.perform(get("/api/v1/webinars/list")
                        .param("category", category)
                        .param("lang", lang)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.empty").value(true))
                .andExpect(jsonPath("$.message").value("Нет запланированных вебинаров"))
                .andExpect(jsonPath("$.totalPages").value(0))
                .andExpect(jsonPath("$.webinars").isArray());

        verify(webinarService).getWebinars(eq(category), eq(lang), any(Pageable.class));
    }

    @Test
    void getWebinars_ShouldReturn400_WhenCategoryMissing() throws Exception {
        mockMvc.perform(get("/api/v1/webinars/list")
                        .param("lang", "ru")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getWebinars_ShouldReturn400_WhenLangIsInvalid() throws Exception {
        mockMvc.perform(get("/api/v1/webinars/list")
                        .param("category", "planned")
                        .param("lang", "de"))  // не входит в список ru|en|uz|uzk|kaa
                .andExpect(status().isBadRequest());
    }

    @Test
    void getWebinars_ShouldReturn400_WhenCategoryBlank() throws Exception {
        mockMvc.perform(get("/api/v1/webinars/list")
                        .param("category", " ")
                        .param("lang", "ru"))
                .andExpect(status().isBadRequest());
    }
}
