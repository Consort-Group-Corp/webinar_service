package uz.consortgroup.webinar_service.exception;

import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
public class NotEnrolledParticipantsException extends RuntimeException {
    private final UUID courseId;
    private final List<String> notEnrolled;

    public NotEnrolledParticipantsException(UUID courseId, List<String> notEnrolled) {
        super("Some participants are not enrolled in the selected course");
        this.courseId = courseId;
        this.notEnrolled = notEnrolled;
    }
}
