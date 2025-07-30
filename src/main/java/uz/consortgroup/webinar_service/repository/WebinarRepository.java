package uz.consortgroup.webinar_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.consortgroup.webinar_service.entity.Webinar;

import java.util.UUID;

public interface WebinarRepository extends JpaRepository<Webinar, UUID> {
}
