package HoangThiMyThanh.QuanLySach.validators;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ValidPasswordValidatorTest {
    private final ValidPasswordValidator validator = new ValidPasswordValidator();

    @Test
    public void testValidPasswords() {
        assertTrue(validator.isValid("Aa1!aaaa", null));
        assertTrue(validator.isValid("StrongP@ssw0rd", null));
    }

    @Test
    public void testInvalidPasswords() {
        // too short
        assertFalse(validator.isValid("Aa1!a", null));
        // no uppercase
        assertFalse(validator.isValid("aa1!aaaa", null));
        // no lowercase
        assertFalse(validator.isValid("AA1!AAAA", null));
        // no digit
        assertFalse(validator.isValid("Aa!aaaaaaaa", null));
        // no special
        assertFalse(validator.isValid("Aa1aaaaaaaa", null));
        // null
        assertFalse(validator.isValid(null, null));
        // blank
        assertFalse(validator.isValid("   ", null));
    }
}