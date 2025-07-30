package uz.consortgroup.webinar_service.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import uz.consortgroup.webinar_service.exception.UnauthorizedException;


import java.util.UUID;

@Component
public class AuthContextImpl implements AuthContext {

    @Override
    public UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("User is not authenticated");
        }

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof AuthenticatedUser(UUID id))) {
            throw new UnauthorizedException("Invalid authentication principal");
        }

        return id;
    }
}
