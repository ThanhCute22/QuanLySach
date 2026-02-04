package HoangThiMyThanh.QuanLySach.utils;

import HoangThiMyThanh.QuanLySach.service.OAuthService;
import HoangThiMyThanh.QuanLySach.service.UserService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.web.SecurityFilterChain;
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
@RequiredArgsConstructor
@lombok.extern.slf4j.Slf4j
public class SecurityConfig {
 private final OAuthService oAuthService;
 private final UserService userService;
 // Password encoder bean
 @Bean
 public PasswordEncoder passwordEncoder() {
     return new BCryptPasswordEncoder();
 }
 @Bean
public SecurityFilterChain securityFilterChain(@NotNull HttpSecurity http,
    org.springframework.beans.factory.ObjectProvider<ClientRegistrationRepository> clientRegProvider
) throws Exception {
    http.authorizeHttpRequests(auth -> auth
            .requestMatchers("/css/**", "/js/**", "/", "/register", "/error", "/oauth2/**", "/oauth2/authorization/**")
            .permitAll()
            .requestMatchers("/books/edit/**", "/books/add", "/books/delete")
            .hasAuthority("ADMIN")
            .requestMatchers("/books", "/cart", "/cart/**", "/api/**")
            .hasAnyAuthority("ADMIN", "USER")
            .requestMatchers("/api/**")
            .hasAnyAuthority("ADMIN", "USER")
            .anyRequest().authenticated()
    )
    .logout(logout -> logout
            .logoutUrl("/logout")
            .logoutSuccessUrl("/login")
            .deleteCookies("JSESSIONID")
            .invalidateHttpSession(true)
            .clearAuthentication(true)
            .permitAll()
    )
    .formLogin(formLogin -> formLogin
            .loginPage("/login")
            .loginProcessingUrl("/login")
            .defaultSuccessUrl("/")
            .failureUrl("/login?error")
            .permitAll()
    )
    .rememberMe(rm -> rm
            .key("hutech")
            .rememberMeCookieName("hutech")
            .tokenValiditySeconds(24 * 60 * 60)
            .userDetailsService(userService)
    )
    .exceptionHandling(ex -> ex
            .accessDeniedPage("/403")
    )
    .sessionManagement(sm -> sm
            .maximumSessions(1)
            .expiredUrl("/login")
    )
    .httpBasic(httpBasic -> httpBasic
            .realmName("hutech")
    );

    if (clientRegProvider.getIfAvailable() != null) {
        log.info("OAuth ClientRegistration found - enabling oauth2Login");
        http.oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .failureUrl("/login?error")
                .userInfoEndpoint(u -> u.userService(oAuthService))
                .successHandler((request, response, authentication) -> {
                    var principal = authentication.getPrincipal();
                    try {
                        String email = null;
                        String name = null;
                        if (principal instanceof DefaultOidcUser oidcUser) {
                            email = (String) oidcUser.getClaims().get("email");
                            name = (String) oidcUser.getClaims().get("name");
                            log.info("OAuth login (OIDC) claims={}", oidcUser.getClaims());
                        } else if (principal instanceof org.springframework.security.oauth2.core.user.OAuth2User oauth2User) {
                            email = (String) oauth2User.getAttributes().get("email");
                            name = (String) oauth2User.getAttributes().get("name");
                            log.info("OAuth login (OAuth2) attrs={}", oauth2User.getAttributes());
                        } else {
                            log.warn("Unknown oauth principal type: {}", principal != null ? principal.getClass() : null);
                        }

                        // Ensure local user exists / updated
                        userService.saveOauthUser(email, name != null ? name : (email != null ? email.split("@")[0] : ""));

                        // Load local user and merge authorities into the current Authentication
                        java.util.Optional<HoangThiMyThanh.QuanLySach.entities.User> localUserOpt = null;
                        if (email != null && !email.isBlank()) {
                            localUserOpt = userService.findByEmail(email);
                        }
                        if ((localUserOpt == null || localUserOpt.isEmpty()) && name != null) {
                            localUserOpt = userService.findByUsername(name);
                        }

                        if (localUserOpt != null && localUserOpt.isPresent()) {
                            var localUser = localUserOpt.get();
                            var localAuthorities = localUser.getAuthorities();
                            // merge authorities
                            var merged = new java.util.ArrayList<java.util.Collection<? extends org.springframework.security.core.GrantedAuthority>>();
                            // existing authorities
                            var existingAuths = java.util.stream.Stream.of(authentication.getAuthorities()).flatMap(java.util.Collection::stream).collect(java.util.stream.Collectors.toList());
                            // But authentication.getAuthorities() is a Collection already - simpler approach
                            var mergedList = new java.util.ArrayList<org.springframework.security.core.GrantedAuthority>();
                            mergedList.addAll(authentication.getAuthorities());
                            mergedList.addAll(localAuthorities.stream().map(a -> new org.springframework.security.core.authority.SimpleGrantedAuthority(a.getAuthority())).toList());
                            // remove duplicates by authority name
                            var distinct = mergedList.stream().collect(java.util.stream.Collectors.toMap(org.springframework.security.core.GrantedAuthority::getAuthority, x -> x, (a,b)->a)).values().stream().toList();

                            // create new Authentication
                            var newAuth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(authentication.getPrincipal(), authentication.getCredentials(), distinct);
                            org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(newAuth);
                            // also store in session
                            try {
                                request.getSession().setAttribute(org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, org.springframework.security.core.context.SecurityContextHolder.getContext());
                            } catch (Exception ex) {
                                log.debug("Could not store security context in session: {}", ex.getMessage());
                            }

                            log.info("Merged authorities for oauth user email='{}' -> authorities={}", email, distinct.stream().map(org.springframework.security.core.GrantedAuthority::getAuthority).toList());
                        }
                    } catch (Exception ex) {
                        log.error("Failed to save oauth user or merge authorities: {}", ex.getMessage(), ex);
                    }
                    response.sendRedirect("/");
                })
        );
    } else {
        log.info("No OAuth ClientRegistration found - oauth2Login disabled");
    }

    return http.build();
}
}
 