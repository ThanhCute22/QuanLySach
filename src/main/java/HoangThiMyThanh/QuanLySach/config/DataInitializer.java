package HoangThiMyThanh.QuanLySach.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import HoangThiMyThanh.QuanLySach.entities.Role;
import HoangThiMyThanh.QuanLySach.entities.User;
import HoangThiMyThanh.QuanLySach.repositories.IRoleRepository;
import HoangThiMyThanh.QuanLySach.repositories.IUserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Component
public class DataInitializer implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private final DataSource dataSource;
    private final IRoleRepository roleRepository;
    private final IUserRepository userRepository;

    public DataInitializer(DataSource dataSource, IRoleRepository roleRepository, IUserRepository userRepository) {
        this.dataSource = dataSource;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM Category");
            ResultSet rs = ps.executeQuery();
            int count = 0;
            if (rs.next()) count = rs.getInt(1);
            if (count == 0) {
                log.info("Category table empty — running db/data-insert.sql to populate sample data");
                ResourceDatabasePopulator populator = new ResourceDatabasePopulator(new ClassPathResource("db/data-insert.sql"));
                populator.execute(dataSource);
                log.info("Sample data inserted from db/data-insert.sql");
            } else {
                log.info("Category table already has {} rows — skipping sample data insertion", count);
            }
        } catch (Exception ex) {
            log.warn("Could not initialize sample data: {}", ex.getMessage());
        }

        // ensure roles and default admin user exist
        try {
            var adminRole = roleRepository.findRoleByName("ADMIN");
            if (adminRole == null) {
                adminRole = new Role();
                adminRole.setName("ADMIN");
                adminRole.setDescription("Administrator role");
                roleRepository.save(adminRole);
                log.info("Created missing role ADMIN");
            }
            var userRole = roleRepository.findRoleByName("USER");
            if (userRole == null) {
                userRole = new Role();
                userRole.setName("USER");
                userRole.setDescription("Default user role");
                roleRepository.save(userRole);
                log.info("Created missing role USER");
            }

            // create admin user if missing
            var adminOpt = userRepository.findByUsername("admin");
            if (adminOpt.isEmpty()) {
                var admin = new User();
                admin.setUsername("admin");
                admin.setEmail("admin@example.com");
                admin.setPassword(new BCryptPasswordEncoder().encode("admin"));
                admin.getRoles().add(adminRole);
                userRepository.save(admin);
                log.info("Created default admin user (username=admin, password=admin)");
            } else {
                // ensure admin has ADMIN role
                var admin = adminOpt.get();
                if (admin.getRoles().stream().noneMatch(r -> "ADMIN".equals(r.getName()))) {
                    admin.getRoles().add(adminRole);
                    userRepository.save(admin);
                    log.info("Assigned ADMIN role to existing admin user");
                }
            }
        } catch (Exception ex) {
            log.warn("Could not initialize roles/admin user: {}", ex.getMessage());
        }

        // Log all users and their roles for debugging
        try {
            var users = userRepository.findAll();
            for (var u : users) {
                var roleNames = u.getRoles().stream().map(r -> r.getName()).toList();
                log.info("User '{}' roles={}", u.getUsername(), roleNames);
            }
        } catch (Exception ex) {
            log.debug("Could not list users: {}", ex.getMessage());
        }
    }
}
