package uz.consortgroup.webinar_service.service.webinar;

import uz.consortgroup.webinar_service.entity.Webinar;
import uz.consortgroup.webinar_service.entity.WebinarParticipant;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface WebinarParticipantService {
    Map<UUID, String> addParticipants(Webinar webinar, List<String> identifiers);
    Map<UUID, String> updateParticipants(Webinar webinar, List<String> identifiers);
    List<WebinarParticipant> getParticipantsByWebinarId(UUID webinarId);

}
