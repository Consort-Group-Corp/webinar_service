package uz.consortgroup.webinar_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import uz.consortgroup.core.api.v1.dto.course.response.course.EnrollmentFilterRequest;
import uz.consortgroup.core.api.v1.dto.user.request.UserBulkSearchRequest;
import uz.consortgroup.core.api.v1.dto.user.response.UserBulkSearchResponse;
import uz.consortgroup.core.api.v1.dto.user.response.UserShortInfoResponseDto;
import uz.consortgroup.webinar_service.config.FeignClientConfig;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@FeignClient(
        name = "user-service",
        contextId = "userClient",
        url = "${user.service.url}",
        configuration = FeignClientConfig.class
)
public interface UserClient {

    @PostMapping("/api/v1/internal/users/bulk-search")
    UserBulkSearchResponse searchUsersBulk(@RequestBody UserBulkSearchRequest request);

    @PostMapping("/api/v1/internal/users/short-info")
    Map<UUID, UserShortInfoResponseDto> getShortInfoBulk(@RequestBody List<UUID> userIds);


    @PostMapping("/enrollments/filter")
    List<UUID> filterEnrolled(@RequestBody EnrollmentFilterRequest req);

    @GetMapping("/search")
    Page<UserShortInfoResponseDto> search(@RequestParam String query,
                                          @RequestParam(required=false) UUID courseId,
                                          @RequestParam(defaultValue="false") boolean onlyCourseParticipants,
                                          Pageable pageable);
}
