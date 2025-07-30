package uz.consortgroup.webinar_service.security;

import java.util.UUID;

public record AuthenticatedUser(UUID id) implements HasAuthContext {
    @Override
    public UUID getId() {
        return id;
    }
}
