package uz.consortgroup.webinar_service.service.webinar;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.consortgroup.core.api.v1.dto.user.enumeration.UserRole;
import uz.consortgroup.core.api.v1.dto.user.request.UserBulkSearchRequest;
import uz.consortgroup.core.api.v1.dto.user.request.UserSearchRequest;
import uz.consortgroup.core.api.v1.dto.user.response.UserBulkSearchResponse;
import uz.consortgroup.core.api.v1.dto.user.response.UserSearchResponse;
import uz.consortgroup.webinar_service.client.UserClient;
import uz.consortgroup.webinar_service.entity.Webinar;
import uz.consortgroup.webinar_service.entity.WebinarParticipant;
import uz.consortgroup.webinar_service.exception.UserNotFoundException;
import uz.consortgroup.webinar_service.repository.WebinarParticipantRepository;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    @Transactional
    public List<WebinarParticipant> updateParticipants(Webinar webinar, List<String> identifiers) {
        log.info("Updating participants for webinar: {}", webinar.getId());

        List<WebinarParticipant> existing = webinarParticipantRepository.findByWebinarId(webinar.getId());
        if (!existing.isEmpty()) {
            webinarParticipantRepository.deleteAll(existing);
            webinarParticipantRepository.flush();
            log.debug("Deleted {} existing participants", existing.size());
        }

        List<String> distinctIdentifiers = identifiers.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .toList();

        if (distinctIdentifiers.isEmpty()) {
            log.warn("No valid identifiers provided for webinar: {}", webinar.getId());
            return List.of();
        }

        List<UserSearchRequest> queries = distinctIdentifiers.stream()
                .map(id -> UserSearchRequest.builder().query(id).build())
                .toList();

        UserBulkSearchRequest request = new UserBulkSearchRequest();
        request.setQueries(queries);

        UserBulkSearchResponse response = userClient.searchUsersBulk(request);

        Map<String, UUID> identifierToUserId = response.getUsers().stream()
                .collect(Collectors.toMap(
                        user -> {
                            String key = user.getEmail() != null && distinctIdentifiers.contains(user.getEmail())
                                    ? user.getEmail()
                                    : user.getPinfl();
                            if (key == null) {
                                throw new UserNotFoundException("User does not contain a valid identifier: " + user.getUserId());
                            }
                            return key;
                        },
                        UserSearchResponse::getUserId
                ));

        List<WebinarParticipant> newParticipants = distinctIdentifiers.stream()
                .map(identifier -> {
                    UUID userId = identifierToUserId.get(identifier);
                    if (userId == null) {
                        throw new UserNotFoundException("User not found by identifier: " + identifier);
                    }
                    WebinarParticipant participant = new WebinarParticipant();
                    participant.setWebinar(webinar);
                    participant.setUserId(userId);
                    participant.setCreatedAt(LocalDateTime.now());
                    return participant;
                })
                .toList();

        webinarParticipantRepository.saveAll(newParticipants);
        log.info("Added {} participants to webinar: {}", newParticipants.size(), webinar.getId());

        return newParticipants;
    }


    @Override
    public List<WebinarParticipant> getParticipantsByWebinarId(UUID webinarId) {
        log.debug("Loading participants from DB for webinarId={}", webinarId);
        return webinarParticipantRepository.findByWebinarId(webinarId);
    }
}

