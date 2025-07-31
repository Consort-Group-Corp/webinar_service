package uz.consortgroup.webinar_service.security;

import java.util.UUID;

public interface AuthContext {
    UUID getCurrentUserId();
}
