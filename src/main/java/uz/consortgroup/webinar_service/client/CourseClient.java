package uz.consortgroup.webinar_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import uz.consortgroup.webinar_service.config.FeignClientConfig;

import java.util.List;
import java.util.UUID;

@FeignClient(
        name = "course-service",
        contextId = "courseClient",
        url = "${course.service.url}",
        configuration = FeignClientConfig.class
)
public interface CourseClient {
    @GetMapping("/internal/courses/{courseId}")
    boolean courseExists(@PathVariable("courseId") UUID courseId);

    @GetMapping("/{courseId}/mentor")
    UUID getMentorId(@PathVariable("courseId") UUID courseId);


    @PostMapping("/{courseId}/enrolled/check")
    List<UUID> checkEnrolled(@PathVariable("courseId") UUID courseId,
                             @RequestBody List<UUID> userIds);
}
