package uz.consortgroup.webinar_service.validator;

import uz.consortgroup.core.api.v1.dto.user.enumeration.UserRole;

import java.util.Set;
import java.util.UUID;

public interface CourseValidationService {
    void validateCourseExists(UUID courseId);
    void assertCourseVisibleForUser(UUID courseId, UUID userId, UserRole role);
    Set<UUID> filterEnrolled(UUID courseId, Set<UUID> userIds);
}
