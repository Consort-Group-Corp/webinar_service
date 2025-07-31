package uz.consortgroup.webinar_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import uz.consortgroup.webinar_service.config.properties.FeignClientConfig;

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
}
