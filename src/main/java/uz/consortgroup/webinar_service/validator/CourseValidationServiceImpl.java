package uz.consortgroup.webinar_service.validator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.consortgroup.core.api.v1.dto.user.enumeration.UserRole;
import uz.consortgroup.webinar_service.client.CourseClient;
import uz.consortgroup.webinar_service.exception.CourseNotFoundException;

import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Slf4j
public class CourseValidationServiceImpl implements CourseValidationService {

    private final CourseClient courseClient;

    public void validateCourseExists(UUID courseId) {
        boolean exists = courseClient.courseExists(courseId);
        if (!exists) {
            log.warn("Course with ID {} not found", courseId);
            throw new CourseNotFoundException(String.format("Course with ID %s not found", courseId));
        }
    }

    @Override
    public void assertCourseVisibleForUser(UUID courseId, UUID userId, UserRole role) {
        switch (role) {
            case MENTOR -> {
                UUID tutorId = courseClient.getMentorId(courseId);
                if (tutorId == null || !tutorId.equals(userId)) {
                    throw new SecurityException("Mentor cannot use a foreign course");
                }
            }
            case ADMIN, SUPER_ADMIN -> {}
            default -> throw new SecurityException("Role has no access to this course");
        }
    }

    @Override
    public Set<UUID> filterEnrolled(UUID courseId, Set<UUID> userIds) {
        return Set.of();
    }
}
