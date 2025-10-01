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
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebinarParticipantServiceImpl implements WebinarParticipantService {

    private final WebinarParticipantRepository webinarParticipantRepository;
    private final UserClient userClient;

    @Override
    public Map<UUID, String> addParticipants(Webinar webinar, List<String> identifiers) {
        log.info("Adding participants to webinar {} by {} identifiers (email or pinfl)", webinar.getId(), identifiers.size());

        List<String> distinct = identifiers.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .toList();

        if (distinct.isEmpty()) {
            log.info("No valid identifiers provided.");
            return Map.of();
        }

        List<UserSearchRequest> queries = distinct.stream()
                .map(id -> UserSearchRequest.builder().query(id).build())
                .toList();

        UserBulkSearchRequest request = UserBulkSearchRequest.builder()
                .queries(queries)
                .build();

        UserBulkSearchResponse bulk = userClient.searchUsersBulk(request);
        List<UserSearchResponse> users = bulk.getUsers();
        log.info("Found {} matching users from user service", users.size());

        List<UserSearchResponse> filtered = users.stream()
                .filter(user -> user.getRole() != UserRole.GUEST_USER)
                .toList();

        Map<UUID, String> idToIdentifier = filtered.stream()
                .collect(Collectors.toMap(
                        UserSearchResponse::getUserId,
                        u -> {
                            String email = u.getEmail();
                            String pinfl = u.getPinfl();
                            if (email != null && distinct.contains(email)) return email;
                            if (pinfl != null && distinct.contains(pinfl)) return pinfl;
                            return email != null ? email : pinfl;
                        },
                        (a, b) -> a
                ));

        Set<UUID> existingUserIds = webinarParticipantRepository.findByWebinarId(webinar.getId()).stream()
                .map(WebinarParticipant::getUserId)
                .collect(Collectors.toSet());

        Set<UUID> newUserIds = new HashSet<>(idToIdentifier.keySet());
        newUserIds.removeAll(existingUserIds);

        if (newUserIds.isEmpty()) {
            log.info("No new participants to add.");
            return Map.of();
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

        return idToIdentifier.entrySet().stream()
                .filter(e -> newUserIds.contains(e.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    @Transactional
    public Map<UUID, String> updateParticipants(Webinar webinar, List<String> identifiers) {
        log.info("Updating participants for webinar: {}", webinar.getId());

        List<WebinarParticipant> existing = webinarParticipantRepository.findByWebinarId(webinar.getId());
        if (!existing.isEmpty()) {
            webinarParticipantRepository.deleteAll(existing);
            webinarParticipantRepository.flush();
            log.debug("Deleted {} existing participants", existing.size());
        }

        List<String> distinct = identifiers == null ? List.of() :
                identifiers.stream()
                        .filter(Objects::nonNull)
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .distinct()
                        .toList();

        if (distinct.isEmpty()) {
            log.warn("No valid identifiers provided for webinar: {}", webinar.getId());
            return Map.of();
        }

        List<UserSearchRequest> queries = distinct.stream()
                .map(id -> UserSearchRequest.builder().query(id).build())
                .toList();

        UserBulkSearchRequest request = UserBulkSearchRequest.builder()
                .queries(queries)
                .build();

        UserBulkSearchResponse response = userClient.searchUsersBulk(request);

        Map<String, UUID> identifierToUserId = response.getUsers().stream()
                .collect(Collectors.toMap(
                        user -> {
                            String key = user.getEmail() != null && distinct.contains(user.getEmail())
                                    ? user.getEmail()
                                    : user.getPinfl();
                            if (key == null) {
                                throw new UserNotFoundException("User does not contain a valid identifier: " + user.getUserId());
                            }
                            return key;
                        },
                        UserSearchResponse::getUserId,
                        (a, b) -> a
                ));

        Map<UUID, String> idToIdentifier = identifierToUserId.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey, (a, b) -> a));

        List<WebinarParticipant> toSave = distinct.stream()
                .map(identifier -> {
                    UUID userId = identifierToUserId.get(identifier);
                    if (userId == null) {
                        throw new UserNotFoundException("User not found by identifier: " + identifier);
                    }
                    return WebinarParticipant.builder()
                            .webinar(webinar)
                            .userId(userId)
                            .createdAt(LocalDateTime.now())
                            .build();
                })
                .toList();

        webinarParticipantRepository.saveAll(toSave);
        log.info("Added {} participants to webinar: {}", toSave.size(), webinar.getId());

        return idToIdentifier;
    }

    @Override
    public List<WebinarParticipant> getParticipantsByWebinarId(UUID webinarId) {
        log.debug("Loading participants from DB for webinarId={}", webinarId);
        return webinarParticipantRepository.findByWebinarId(webinarId);
    }
}
