package uz.consortgroup.webinar_service.service.webinar;

import uz.consortgroup.webinar_service.entity.Webinar;

import java.util.List;
import java.util.UUID;

public interface WebinarParticipantService {
    List<UUID> addParticipants(Webinar webinar, List<String> identifiers);
}
