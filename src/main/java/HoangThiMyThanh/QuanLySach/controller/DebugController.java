package HoangThiMyThanh.QuanLySach.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/debug")
public class DebugController {
    @GetMapping("/principal")
    public Map<String, Object> principal(Authentication auth) {
        if (auth == null) return Map.of("authenticated", false);
        List<String> roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        return Map.of(
                "authenticated", true,
                "username", auth.getName(),
                "roles", roles
        );
    }
}
