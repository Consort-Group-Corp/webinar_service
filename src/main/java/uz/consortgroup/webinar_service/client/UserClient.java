package uz.consortgroup.webinar_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import uz.consortgroup.core.api.v1.dto.user.request.UserBulkSearchRequest;
import uz.consortgroup.core.api.v1.dto.user.response.UserBulkSearchResponse;
import uz.consortgroup.core.api.v1.dto.user.response.UserShortInfoResponseDto;
import uz.consortgroup.webinar_service.config.properties.FeignClientConfig;

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
}
