package uz.consortgroup.webinar_service.service.strategy;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import uz.consortgroup.core.api.v1.dto.user.enumeration.UserRole;
import uz.consortgroup.webinar_service.entity.Webinar;

import java.util.UUID;

public interface WebinarCategoryStrategy {
    Specification<Webinar> getSpecification(UUID userId, UserRole role);
    Sort getSort();
}
