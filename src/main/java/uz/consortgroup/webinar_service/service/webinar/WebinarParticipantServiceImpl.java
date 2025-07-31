package uz.consortgroup.webinar_service.service.webinar;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.consortgroup.core.api.v1.dto.user.enumeration.UserRole;
import uz.consortgroup.core.api.v1.dto.user.request.UserBulkSearchRequest;
import uz.consortgroup.core.api.v1.dto.user.request.UserSearchRequest;
import uz.consortgroup.core.api.v1.dto.user.response.UserSearchResponse;
import uz.consortgroup.webinar_service.client.UserClient;
import uz.consortgroup.webinar_service.entity.Webinar;
import uz.consortgroup.webinar_service.entity.WebinarParticipant;
import uz.consortgroup.webinar_service.repository.WebinarParticipantRepository;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebinarParticipantServiceImpl implements WebinarParticipantService {

    private final WebinarParticipantRepository webinarParticipantRepository;
    private final UserClient userClient;

    @Override
    public List<UUID> addParticipants(Webinar webinar, List<String> identifiers) {
        log.info("Adding participants to webinar {} by {} identifiers (email or pinfl)", webinar.getId(), identifiers.size());

        List<UserSearchRequest> queries = identifiers.stream()
                .map(id -> UserSearchRequest.builder().query(id).build())
                .toList();

        UserBulkSearchRequest request = UserBulkSearchRequest.builder()
                .queries(queries)
                .build();

        List<UserSearchResponse> users = userClient.searchUsersBulk(request).getUsers();

        log.info("Found {} matching users from user service", users.size());

        List<UserSearchResponse> filtered = users.stream()
                .filter(user -> user.getRole() != UserRole.GUEST_USER)
                .toList();

        Set<UUID> newUserIds = filtered.stream()
                .map(UserSearchResponse::getUserId)
                .collect(Collectors.toSet());

        Set<UUID> existingUserIds = webinarParticipantRepository.findByWebinarId(webinar.getId()).stream()
                .map(WebinarParticipant::getUserId)
                .collect(Collectors.toSet());

        newUserIds.removeAll(existingUserIds);

        if (newUserIds.isEmpty()) {
            log.info("No new participants to add.");
            return List.of();
        }

        List<WebinarParticipant> participants = newUserIds.stream()
                .map(userId -> WebinarParticipant.builder()
                        .webinar(webinar)
                        .userId(userId)
                        .createdAt(LocalDateTime.now())
                        .build())
                .toList();

        webinarParticipantRepository.saveAll(participants);

        log.info("Successfully added {} new participants", participants.size());
        return newUserIds.stream().toList();
    }

    @Override
    public List<UUID> updateParticipants(Webinar webinar, List<String> identifiers) {
        log.info("Updating participants for webinar: {}", webinar.getId());

        List<WebinarParticipant> existing = webinarParticipantRepository.findByWebinarId(webinar.getId());
        if (!existing.isEmpty()) {
            webinarParticipantRepository.deleteAll(existing);
            log.debug("Deleted {} existing participants", existing.size());
        }

        List<UUID> addedUserIds = addParticipants(webinar, identifiers);
        log.info("Updated participants for webinar: {}, added {} new", webinar.getId(), addedUserIds.size());

        return addedUserIds;
    }

    @Override
    public List<WebinarParticipant> getParticipantsByWebinarId(UUID webinarId) {
        log.debug("Loading participants from DB for webinarId={}", webinarId);
        return webinarParticipantRepository.findByWebinarId(webinarId);
    }
}

