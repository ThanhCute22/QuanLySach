package HoangThiMyThanh.QuanLySach.validators;

import HoangThiMyThanh.QuanLySach.validators.annotations.ValidPassword;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;

@Component
public class ValidPasswordValidator implements ConstraintValidator<ValidPassword, String> {

    // At least 8 chars, one lowercase, one uppercase, one digit, one special char
    private static final String PASSWORD_PATTERN = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$";

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null || password.isBlank()) return false;
        return password.matches(PASSWORD_PATTERN);
    }
}