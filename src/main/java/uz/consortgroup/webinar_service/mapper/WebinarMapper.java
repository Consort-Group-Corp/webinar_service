package uz.consortgroup.webinar_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import uz.consortgroup.core.api.v1.dto.webinar.response.WebinarResponseDto;
import uz.consortgroup.webinar_service.entity.Webinar;
import uz.consortgroup.webinar_service.entity.WebinarParticipant;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface WebinarMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "title", source = "title")
    @Mapping(target = "category", source = "category")
    @Mapping(target = "startTime", source = "startTime")
    @Mapping(target = "endTime", source = "endTime")
    @Mapping(target = "platformUrl", source = "platformUrl")
    @Mapping(target = "courseId", source = "courseId")
    @Mapping(target = "languageCode", source = "languageCode")
    @Mapping(target = "onlyCourseParticipants", source = "onlyCourseParticipants")
    @Mapping(target = "participants", source = "participants", qualifiedByName = "mapParticipants")
    @Mapping(target = "createdBy", source = "createdBy")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")

    @Mapping(target = "previewUrl", source = "previewUrl")
    @Mapping(target = "previewFilename", source = "previewFilename")
    WebinarResponseDto toDto(Webinar webinar);

    @Named("mapParticipants")
    default Set<String> mapParticipants(List<WebinarParticipant> participants) {
        if (participants == null || participants.isEmpty()) return new LinkedHashSet<>();
        return participants.stream()
                .map(WebinarParticipant::getUserId)
                .filter(Objects::nonNull)
                .map(Object::toString)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
