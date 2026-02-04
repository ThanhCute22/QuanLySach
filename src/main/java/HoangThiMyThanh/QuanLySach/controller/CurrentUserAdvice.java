package HoangThiMyThanh.QuanLySach.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Objects;
import java.util.stream.Collectors;

@ControllerAdvice
public class CurrentUserAdvice {
    @ModelAttribute("currentAuthorities")
    public String currentAuthorities(Authentication authentication) {
        if (authentication == null) return "";
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(Objects::nonNull)
                .collect(Collectors.joining(", "));
    }

    @ModelAttribute("currentUser")
    public String currentUser(Authentication authentication) {
        if (authentication == null) return "";
        var principal = authentication.getPrincipal();
        try {
            if (principal instanceof org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser oidc) {
                String email = (String) oidc.getClaims().get("email");
                String name = (String) oidc.getClaims().get("name");
                return (email != null && !email.isBlank()) ? email : (name != null ? name : authentication.getName());
            } else if (principal instanceof org.springframework.security.oauth2.core.user.OAuth2User oauth2) {
                String email = (String) oauth2.getAttributes().get("email");
                String name = (String) oauth2.getAttributes().get("name");
                return (email != null && !email.isBlank()) ? email : (name != null ? name : authentication.getName());
            } else {
                return authentication.getName();
            }
        } catch (Exception ex) {
            return authentication.getName();
        }
    }
}