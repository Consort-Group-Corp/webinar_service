package uz.consortgroup.webinar_service.service.strategy;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import uz.consortgroup.core.api.v1.dto.user.enumeration.UserRole;
import uz.consortgroup.webinar_service.entity.Webinar;
import uz.consortgroup.webinar_service.specification.WebinarSpecification;

import java.time.LocalDateTime;
import java.util.UUID;

@Component("past")
public class PastWebinarStrategy implements WebinarCategoryStrategy {

    @Override
    public Specification<Webinar> getSpecification(UUID userId, UserRole role) {
        Specification<Webinar> spec = WebinarSpecification.isPast(LocalDateTime.now());

        if (role == UserRole.MENTOR) {
            spec = spec.and(WebinarSpecification.createdBy(userId));
        }

        return spec;
    }

    @Override
    public Sort getSort() {
        return Sort.by("endTime").descending();
    }
}
