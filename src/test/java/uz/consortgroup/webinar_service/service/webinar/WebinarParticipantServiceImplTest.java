package uz.consortgroup.webinar_service.service.webinar;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.consortgroup.core.api.v1.dto.user.enumeration.UserRole;
import uz.consortgroup.core.api.v1.dto.user.request.UserBulkSearchRequest;
import uz.consortgroup.core.api.v1.dto.user.response.UserBulkSearchResponse;
import uz.consortgroup.core.api.v1.dto.user.response.UserSearchResponse;
import uz.consortgroup.webinar_service.client.UserClient;
import uz.consortgroup.webinar_service.entity.Webinar;
import uz.consortgroup.webinar_service.entity.WebinarParticipant;
import uz.consortgroup.webinar_service.exception.UserNotFoundException;
import uz.consortgroup.webinar_service.repository.WebinarParticipantRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WebinarParticipantServiceImplTest {

    @Mock
    private WebinarParticipantRepository webinarParticipantRepository;

    @Mock
    private UserClient userClient;

    @InjectMocks
    private WebinarParticipantServiceImpl service;

    private Webinar webinar;

    @BeforeEach
    void setUp() {
        webinar = new Webinar();
        webinar.setId(UUID.randomUUID());
    }

    @Test
    void addParticipants_shouldAddOnlyNonGuestAndNotExisting() {
        String email = "test@example.com";
        UUID userId = UUID.randomUUID();

        UserSearchResponse validUser = new UserSearchResponse();
        validUser.setUserId(userId);
        validUser.setEmail(email);
        validUser.setRole(UserRole.STUDENT);

        UserSearchResponse guestUser = new UserSearchResponse();
        guestUser.setUserId(UUID.randomUUID());
        guestUser.setEmail("guest@example.com");
        guestUser.setRole(UserRole.GUEST_USER);

        when(userClient.searchUsersBulk(any(UserBulkSearchRequest.class)))
                .thenReturn(new UserBulkSearchResponse(List.of(validUser, guestUser)));

        when(webinarParticipantRepository.findByWebinarId(webinar.getId())).thenReturn(emptyList());

        List<UUID> result = service.addParticipants(webinar, List.of(email));

        assertThat(result).containsExactly(userId);

        ArgumentCaptor<List<WebinarParticipant>> captor = ArgumentCaptor.forClass(List.class);
        verify(webinarParticipantRepository).saveAll(captor.capture());

        List<WebinarParticipant> saved = captor.getValue();
        assertThat(saved).hasSize(1);
        assertThat(saved.get(0).getUserId()).isEqualTo(userId);
    }

    @Test
    void addParticipants_shouldReturnEmpty_whenNoValidUsers() {
        when(userClient.searchUsersBulk(any())).thenReturn(new UserBulkSearchResponse(emptyList()));
        when(webinarParticipantRepository.findByWebinarId(any())).thenReturn(emptyList());

        List<UUID> result = service.addParticipants(webinar, List.of("nonexistent@example.com"));

        assertThat(result).isEmpty();
        verify(webinarParticipantRepository, never()).saveAll(any());
    }

    @Test
    void updateParticipants_shouldReplaceParticipants() {
        UUID userId = UUID.randomUUID();
        String email = "user@example.com";

        WebinarParticipant existing = WebinarParticipant.builder()
                .webinar(webinar)
                .userId(UUID.randomUUID())
                .createdAt(LocalDateTime.now())
                .build();

        when(webinarParticipantRepository.findByWebinarId(webinar.getId())).thenReturn(List.of(existing));

        UserSearchResponse found = new UserSearchResponse();
        found.setUserId(userId);
        found.setEmail(email);
        found.setRole(UserRole.STUDENT);

        UserBulkSearchResponse bulkResponse = new UserBulkSearchResponse(List.of(found));
        when(userClient.searchUsersBulk(any())).thenReturn(bulkResponse);

        List<WebinarParticipant> result = service.updateParticipants(webinar, List.of(email));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo(userId);

        verify(webinarParticipantRepository).deleteAll(List.of(existing));
        verify(webinarParticipantRepository).saveAll(any());
    }

    @Test
    void updateParticipants_shouldThrow_whenIdentifierNotFound() {
        when(webinarParticipantRepository.findByWebinarId(any())).thenReturn(emptyList());

        UserSearchResponse found = new UserSearchResponse();
        found.setUserId(UUID.randomUUID());
        found.setEmail("other@example.com");
        found.setRole(UserRole.STUDENT);

        UserBulkSearchResponse bulkResponse = new UserBulkSearchResponse(List.of(found));
        when(userClient.searchUsersBulk(any())).thenReturn(bulkResponse);

        assertThrows(UserNotFoundException.class, () -> {
            service.updateParticipants(webinar, List.of("notfound@example.com"));
        });
    }

    @Test
    void getParticipantsByWebinarId_shouldReturnFromRepository() {
        UUID userId = UUID.randomUUID();

        WebinarParticipant p = WebinarParticipant.builder()
                .webinar(webinar)
                .userId(userId)
                .createdAt(LocalDateTime.now())
                .build();

        when(webinarParticipantRepository.findByWebinarId(webinar.getId()))
                .thenReturn(List.of(p));

        List<WebinarParticipant> result = service.getParticipantsByWebinarId(webinar.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo(userId);
    }
}
