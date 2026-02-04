package HoangThiMyThanh.QuanLySach.service;

import HoangThiMyThanh.QuanLySach.entities.User;
import HoangThiMyThanh.QuanLySach.repositories.IUserRepository;
import HoangThiMyThanh.QuanLySach.repositories.IRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
@Slf4j
public class UserService implements UserDetailsService {
    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private IRoleRepository roleRepository;

    @Transactional(isolation = Isolation.SERIALIZABLE,
            rollbackFor = {Exception.class, Throwable.class})
    public void save(@NotNull User user) {
        user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
        userRepository.save(user);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE,
            rollbackFor = {Exception.class, Throwable.class})
    public void setDefaultRole(String username){
        var optUser = userRepository.findByUsername(username);
        if (optUser.isPresent()) {
            var u = optUser.get();
            var role = roleRepository.findRoleById(HoangThiMyThanh.QuanLySach.constants.Role.USER.value);
            if (role == null) {
                role = roleRepository.findRoleByName("USER");
            }
            if (role != null) {
                u.getRoles().add(role);
            }
        }
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @org.springframework.transaction.annotation.Transactional
    public void saveOauthUser(String email, @NotNull String username) {
        try {
            // If we've already got a user for this email, update roles/provider and return
            if (email != null && !email.isBlank()) {
                var existingByEmail = userRepository.findByEmail(email);
                if (existingByEmail.isPresent()) {
                    var u = existingByEmail.get();
                    if (u.getProvider() == null || u.getProvider().isBlank()) u.setProvider(HoangThiMyThanh.QuanLySach.constants.Provider.GOOGLE.value);
                    // ensure USER role exists and is assigned
                    var role = roleRepository.findRoleById(HoangThiMyThanh.QuanLySach.constants.Role.USER.value);
                    if (role == null) role = roleRepository.findRoleByName("USER");
                    if (role == null) {
                        var newRole = new HoangThiMyThanh.QuanLySach.entities.Role();
                        newRole.setName("USER");
                        role = roleRepository.save(newRole);
                    }
                    if (role != null && u.getRoles().stream().noneMatch(r -> "USER".equals(r.getName()))) u.getRoles().add(role);
                    userRepository.save(u);
                    log.info("OAuth login: existing user by email='{}' -> id={} roles={}", email, u.getId(), u.getRoles().stream().map(r -> r.getName()).toList());
                    return;
                }
            }

            // Normalize username: prefer non-numeric, otherwise derive from email
            if (username == null || username.isBlank() || username.matches("^\\d+$")) {
                if (email != null && email.contains("@")) {
                    username = email.split("@", 2)[0];
                } else {
                    username = "user" + System.currentTimeMillis();
                }
            }

            if (userRepository.findByUsername(username).isPresent()) {
                var existing = userRepository.findByUsername(username).get();
                if ((existing.getEmail() == null || existing.getEmail().isBlank()) && email != null) existing.setEmail(email);
                if (existing.getProvider() == null || existing.getProvider().isBlank()) existing.setProvider(HoangThiMyThanh.QuanLySach.constants.Provider.GOOGLE.value);
                userRepository.save(existing);
                log.info("OAuth login: matched existing user by username='{}' -> id={} roles={}", username, existing.getId(), existing.getRoles().stream().map(r -> r.getName()).toList());
                return;
            }

            var user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setPassword(new BCryptPasswordEncoder().encode(username));
            user.setProvider(HoangThiMyThanh.QuanLySach.constants.Provider.GOOGLE.value);
            // Try to add USER role; create it if missing
            var role = roleRepository.findRoleById(HoangThiMyThanh.QuanLySach.constants.Role.USER.value);
            if (role == null) {
                role = roleRepository.findRoleByName("USER");
            }
            if (role == null) {
                var newRole = new HoangThiMyThanh.QuanLySach.entities.Role();
                newRole.setName("USER");
                role = roleRepository.save(newRole);
            }
            if (role != null) user.getRoles().add(role);
            var saved = userRepository.save(user);
            log.info("OAuth login: created user username='{}' email='{}' id={} roles={}", username, email, saved.getId(), saved.getRoles().stream().map(r -> r.getName()).toList());
        } catch (Exception ex) {
            log.error("Failed to save oauth user: {}", ex.getMessage(), ex);
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Attempting to load user for authentication: {}", username);
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        try {
            var authorities = user.getAuthorities()
                    .stream()
                    .map(GrantedAuthority::getAuthority)
                    .filter(java.util.Objects::nonNull)
                    .toArray(String[]::new);
            return org.springframework.security.core.userdetails.User
                    .withUsername(user.getUsername())
                    .password(user.getPassword())
                    .authorities(authorities)
                    .accountExpired(false)
                    .accountLocked(false)
                    .credentialsExpired(false)
                    .disabled(false)
                    .build();
        } catch (Exception ex) {
            log.error("Failed to build UserDetails for username {}: {}", username, ex.getMessage(), ex);
            throw new UsernameNotFoundException("User data invalid");
        }
    }
}
