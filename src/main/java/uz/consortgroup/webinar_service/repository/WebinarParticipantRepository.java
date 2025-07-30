package uz.consortgroup.webinar_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.consortgroup.webinar_service.entity.WebinarParticipant;

import java.util.UUID;

@Repository
public interface WebinarParticipantRepository extends JpaRepository<WebinarParticipant, UUID> {
}
