package uz.consortgroup.webinar_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "webinar_participants", schema = "webinar_schema",
       uniqueConstraints = {
           @UniqueConstraint(name = "uq_webinar_user", columnNames = {"webinar_id", "user_id"})
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebinarParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "webinar_id", nullable = false, foreignKey = @ForeignKey(name = "fk_webinar"))
    private Webinar webinar;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
