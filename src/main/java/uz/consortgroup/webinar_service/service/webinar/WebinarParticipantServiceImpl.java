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
import java.util.UUID;

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

        int excluded = users.size() - filtered.size();
        if (excluded > 0) {
            log.info("Excluded {} GUEST_USER(s)", excluded);
        }

        List<WebinarParticipant> participants = filtered.stream()
                .map(user -> WebinarParticipant.builder()
                        .webinar(webinar)
                        .userId(user.getUserId())
                        .createdAt(LocalDateTime.now())
                        .build())
                .toList();

        webinarParticipantRepository.saveAll(participants);

        List<UUID> addedUserIds = participants.stream()
                .map(WebinarParticipant::getUserId)
                .toList();

        log.info("Successfully added {} participants to webinar {}", addedUserIds.size(), webinar.getId());
        addedUserIds.forEach(id ->
                log.debug("Participant added: userId={} to webinarId={}", id, webinar.getId())
        );

        return addedUserIds;
    }
}
