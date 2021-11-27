package org.secureapp.util;

import org.secureapp.config.SecuredUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class LoggedInUserHelper {

    public Long getCurrentLoggedInUserID() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        SecuredUserDetails userDetails = (SecuredUserDetails) authentication.getPrincipal();
        return userDetails.getUserId();
    }

    public String getCurrentLoggedInUserName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        SecuredUserDetails userDetails = (SecuredUserDetails) authentication.getPrincipal();
        return userDetails.getUsername();
    }

    public boolean isCurrentLoggedInUserAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        return authentication
                .getAuthorities()
                .stream()
                .anyMatch(role -> role.getAuthority().equals("ROLE_ADMIN"));
    }

    public boolean checkCurrentUserEqualUserNameParam(String userName) {
        return userName.equalsIgnoreCase(getCurrentLoggedInUserName());
    }
}
