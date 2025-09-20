package uz.consortgroup.webinar_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import uz.consortgroup.core.api.v1.dto.webinar.request.WebinarCreateRequestDto;
import uz.consortgroup.core.api.v1.dto.webinar.request.WebinarUpdateRequestDto;
import uz.consortgroup.core.api.v1.dto.webinar.response.WebinarListPageResponse;
import uz.consortgroup.core.api.v1.dto.webinar.response.WebinarResponseDto;
import uz.consortgroup.webinar_service.service.webinar.WebinarService;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/webinars")
@Validated
@Tag(name = "Webinars", description = "Операции с вебинарами")
public class WebinarController {

    private static final String FILE_RULES =
            "Файл превью (часть 'file'), опционально. Допустимые типы: image/jpeg, image/png. " +
                    "Расширения: jpg, jpeg, png. Максимальный размер: 100MB.";

    private final WebinarService webinarService;

    @Operation(
            summary = "Создать вебинар",
            description = "Создает вебинар. Принимает multipart/form-data: JSON-метаданные в части 'metadata' и опциональный файл превью в части 'file'.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Создано", content = @Content(schema = @Schema(implementation = WebinarResponseDto.class))),
                    @ApiResponse(responseCode = "400", description = "Ошибка валидации"),
                    @ApiResponse(responseCode = "401", description = "Неавторизован"),
                    @ApiResponse(responseCode = "403", description = "Нет доступа"),
                    @ApiResponse(responseCode = "413", description = "Размер файла превышает лимит (100MB)"),
                    @ApiResponse(responseCode = "415", description = "Неподдерживаемый тип файла (разрешены: image/jpeg, image/png)")
            }
    )
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public WebinarResponseDto createWebinar(
            @Parameter(
                    description = "JSON WebinarCreateRequestDto (часть 'metadata')",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = WebinarCreateRequestDto.class),
                            examples = @ExampleObject(
                                    name = "Пример metadata",
                                    value = """
                                            {
                                              "title": "Введение в Spring Boot",
                                              "category": "planned",
                                              "startTime": "2025-08-20T10:00:00",
                                              "endTime": "2025-08-20T11:00:00",
                                              "platformUrl": "https://meet.example.com/room-123",
                                              "courseId": "df25826b-3c90-4e22-a820-224d4cfb85fa",
                                              "languageCode": "RU",
                                              "participants": ["user1@example.com","user2@example.com"]
                                            }
                                            """
                            )
                    )
            )
            @RequestPart("metadata") @Valid WebinarCreateRequestDto metadata,

            @Parameter(
                    description = FILE_RULES,
                    content = {
                            @Content(mediaType = "image/jpeg", schema = @Schema(type = "string", format = "binary")),
                            @Content(mediaType = "image/png",  schema = @Schema(type = "string", format = "binary"))
                    }
            )
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {
        return webinarService.createWebinar(metadata, file);
    }

    @Operation(
            summary = "Обновить вебинар",
            description = "Обновляет вебинар. Принимает multipart/form-data: JSON-метаданные в части 'metadata' (WebinarUpdateRequestDto) и опциональный файл превью в части 'file'.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = WebinarResponseDto.class))),
                    @ApiResponse(responseCode = "400", description = "Ошибка валидации"),
                    @ApiResponse(responseCode = "401", description = "Не авторизован"),
                    @ApiResponse(responseCode = "403", description = "Нет доступа"),
                    @ApiResponse(responseCode = "404", description = "Вебинар не найден"),
                    @ApiResponse(responseCode = "413", description = "Размер файла превышает лимит (100MB)"),
                    @ApiResponse(responseCode = "415", description = "Неподдерживаемый тип файла (разрешены: image/jpeg, image/png)")
            }
    )
    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public WebinarResponseDto updateWebinar(
            @Parameter(
                    description = "JSON WebinarUpdateRequestDto (часть 'metadata')",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = WebinarUpdateRequestDto.class),
                            examples = @ExampleObject(
                                    name = "Пример metadata",
                                    value = """
                                            {
                                              "id": "9c7bfe69-920f-43e2-af84-f6bfc4b8cd83",
                                              "title": "Spring Boot: продвинутые темы",
                                              "category": "planned",
                                              "startTime": "2025-08-22T10:00:00",
                                              "endTime": "2025-08-22T11:30:00",
                                              "platformUrl": "https://meet.example.com/room-789",
                                              "courseId": "df25826b-3c90-4e22-a820-224d4cfb85fa",
                                              "languageCode": "RU",
                                              "participants": ["user3@example.com"]
                                            }
                                            """
                            )
                    )
            )
            @RequestPart("metadata") WebinarUpdateRequestDto metadataJson,

            @Parameter(
                    description = FILE_RULES,
                    content = {
                            @Content(mediaType = "image/jpeg", schema = @Schema(type = "string", format = "binary")),
                            @Content(mediaType = "image/png",  schema = @Schema(type = "string", format = "binary"))
                    }
            )
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {

        return webinarService.updateWebinar(metadataJson, file);
    }

    @Operation(
            summary = "Удалить вебинар",
            description = "Удаляет вебинар по ID.",
            parameters = {
                    @Parameter(name = "webinarId", description = "UUID вебинара", required = true, example = "9c7bfe69-920f-43e2-af84-f6bfc4b8cd83")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Удалено"),
                    @ApiResponse(responseCode = "401", description = "Неавторизован"),
                    @ApiResponse(responseCode = "403", description = "Нет доступа"),
                    @ApiResponse(responseCode = "404", description = "Не найден")
            }
    )
    @DeleteMapping("/{webinarId}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteWebinar(@PathVariable UUID webinarId) {
        webinarService.deleteWebinar(webinarId);
    }

    @Operation(
            summary = "Список вебинаров (пагинация)",
            description = """
                    Возвращает список вебинаров по категории и языку с пагинацией.
                    Параметры пагинации: page (0..N), size, sort (например, sort=startTime,desc)
                    """,
            parameters = {
                    @Parameter(name = "category", required = true, description = "Категория вебинара", example = "planned"),
                    @Parameter(name = "lang", description = "Код языка интерфейса", example = "ru",
                            schema = @Schema(allowableValues = {"ru", "en", "uz", "uzk", "kaa"}))
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = WebinarListPageResponse.class))),
                    @ApiResponse(responseCode = "401", description = "Неавторизован"),
                    @ApiResponse(responseCode = "403", description = "Нет доступа")
            }
    )
    @GetMapping("/list")
    @ResponseStatus(HttpStatus.OK)
    public WebinarListPageResponse getWebinars(
            @RequestParam @NotBlank String category,
            @RequestParam(defaultValue = "ru") @Pattern(regexp = "ru|en|uz|uzk|kaa") String lang,
            @ParameterObject @PageableDefault(size = 10) @Valid Pageable pageable
    ) {
        return webinarService.getWebinars(category, lang, pageable);
    }
}
