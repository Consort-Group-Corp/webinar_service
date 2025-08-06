package uz.consortgroup.webinar_service.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.consortgroup.webinar_service.entity.Webinar;

import java.util.Optional;
import java.util.UUID;

public interface WebinarRepository extends JpaRepository<Webinar, UUID>, JpaSpecificationExecutor<Webinar> {
    @EntityGraph(attributePaths = "participants")
    @Query("SELECT w FROM Webinar w WHERE w.id = :id")
    Optional<Webinar> findByIdWithParticipants(@Param("id") UUID id);
}
