package uz.consortgroup.webinar_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.consortgroup.webinar_service.entity.WebinarParticipant;

import java.util.List;
import java.util.UUID;

@Repository
public interface WebinarParticipantRepository extends JpaRepository<WebinarParticipant, UUID> {
    @Query("SELECT p FROM WebinarParticipant p WHERE p.webinar.id = :webinarId")
    List<WebinarParticipant> findByWebinarId(@Param("webinarId") UUID webinarId);
}
