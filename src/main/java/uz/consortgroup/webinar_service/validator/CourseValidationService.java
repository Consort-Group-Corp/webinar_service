package uz.consortgroup.webinar_service.validator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.consortgroup.webinar_service.client.CourseClient;
import uz.consortgroup.webinar_service.exception.CourseNotFoundException;

import java.util.UUID;

@RequiredArgsConstructor
@Service
@Slf4j
public class CourseValidationService {

    private final CourseClient courseClient;

    public void validateCourseExists(UUID courseId) {
        boolean exists = courseClient.courseExists(courseId);
        if (!exists) {
            log.warn("Course with ID {} not found", courseId);
            throw new CourseNotFoundException(String.format("Course with ID %s not found", courseId));
        }
    }
}
