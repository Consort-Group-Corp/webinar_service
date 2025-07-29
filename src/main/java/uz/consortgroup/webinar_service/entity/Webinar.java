package uz.consortgroup.webinar_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "webinars", schema = "webinar_schema")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Webinar {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(name = "preview_url")
    private String previewUrl;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "platform_url", nullable = false)
    private String platformUrl;

    @Column(name = "course_id", nullable = false)
    private UUID courseId;

    @Column(name = "language_code", nullable = false, length = 50)
    private String languageCode;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "webinar", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WebinarParticipant> participants;
}
