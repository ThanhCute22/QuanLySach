package HoangThiMyThanh.QuanLySach.validators;

import HoangThiMyThanh.QuanLySach.service.UserService;
import HoangThiMyThanh.QuanLySach.validators.annotations.ValidUsername;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ValidUsernameValidator implements ConstraintValidator<ValidUsername, String> {

    @Autowired
    private UserService userService;

    @Override
    public boolean isValid(String username, ConstraintValidatorContext context) {
        if (username == null || username.isBlank()) {
            return false; // username required
        }
        // If userService not yet injected for some reason, allow to pass to avoid NPE
        if (userService == null) {
            return true;
        }
        return userService.findByUsername(username).isEmpty();
    }
}
