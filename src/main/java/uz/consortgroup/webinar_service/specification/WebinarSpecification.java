package uz.consortgroup.webinar_service.specification;

import org.springframework.data.jpa.domain.Specification;
import uz.consortgroup.webinar_service.entity.Webinar;

import java.time.LocalDateTime;
import java.util.UUID;

public class WebinarSpecification {

    public static Specification<Webinar> isPlanned(LocalDateTime now) {
        return (root, query, cb) -> cb.greaterThan(root.get("startTime"), now);
    }

    public static Specification<Webinar> isPast(LocalDateTime now) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("endTime"), now);
    }

    public static Specification<Webinar> createdBy(UUID tutorId) {
        return (root, query, cb) -> cb.equal(root.get("createdBy"), tutorId);
    }
}
