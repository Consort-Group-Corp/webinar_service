package uz.consortgroup.webinar_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import uz.consortgroup.core.api.v1.dto.webinar.response.WebinarResponseDto;
import uz.consortgroup.webinar_service.entity.Webinar;
import uz.consortgroup.webinar_service.entity.WebinarParticipant;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface WebinarMapper {
    @Mapping(target = "id", source = "id")
    @Mapping(target = "title", source = "title")
    @Mapping(target = "startTime", source = "startTime")
    @Mapping(target = "endTime", source = "endTime")
    @Mapping(target = "platformUrl", source = "platformUrl")
    @Mapping(target = "courseId", source = "courseId")
    @Mapping(target = "languageCode", source = "languageCode")
    @Mapping(target = "participants", source = "participants", qualifiedByName = "mapParticipants")
    @Mapping(target = "createdBy", source = "createdBy")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    WebinarResponseDto toDto(Webinar webinar);

    @Named("mapParticipants")
    default List<String> mapParticipants(List<WebinarParticipant> participants) {
        return participants == null ? null :
                participants.stream()
                        .map(p -> p.getUserId().toString())
                        .collect(Collectors.toList());
    }
}
