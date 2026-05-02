package com.example.BlogApp.utils.fieldValidators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Validator for strong password requirements.
 * Checks:
 * 1. Minimum length (8 characters)
 * 2. At least one uppercase letter
 * 3. At least one lowercase letter
 * 4. At least one digit
 * 5. At least one special character
 * 6. Not in the common passwords list
 */
@Slf4j
public class StrongPasswordValidator implements ConstraintValidator<StrongPassword, String> {

    private static final int MIN_LENGTH = 8;
    private static final Pattern UPPERCASE = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE = Pattern.compile("[a-z]");
    private static final Pattern DIGIT = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL_CHAR = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>?/`~]");

    // Common passwords to avoid
    private static final Set<String> COMMON_PASSWORDS = new HashSet<>();

    static {
        // Common weak passwords to prevent
        COMMON_PASSWORDS.add("password");
        COMMON_PASSWORDS.add("password123");
        COMMON_PASSWORDS.add("password1");
        COMMON_PASSWORDS.add("123456");
        COMMON_PASSWORDS.add("1234567890");
        COMMON_PASSWORDS.add("12345678");
        COMMON_PASSWORDS.add("abc123");
        COMMON_PASSWORDS.add("admin123");
        COMMON_PASSWORDS.add("qwerty");
        COMMON_PASSWORDS.add("qwerty123");
        COMMON_PASSWORDS.add("letmein");
        COMMON_PASSWORDS.add("welcome");
        COMMON_PASSWORDS.add("monkey");
        COMMON_PASSWORDS.add("dragon");
        COMMON_PASSWORDS.add("master");
        COMMON_PASSWORDS.add("sunshine");
        COMMON_PASSWORDS.add("princess");
        COMMON_PASSWORDS.add("football");
        COMMON_PASSWORDS.add("shadow");
        COMMON_PASSWORDS.add("123123");
        COMMON_PASSWORDS.add("passw0rd");
        COMMON_PASSWORDS.add("pass123");
        COMMON_PASSWORDS.add("admin");
        COMMON_PASSWORDS.add("root");
        COMMON_PASSWORDS.add("toor");
        COMMON_PASSWORDS.add("test");
        COMMON_PASSWORDS.add("guest");
        COMMON_PASSWORDS.add("info");
        COMMON_PASSWORDS.add("info123");
    }

    @Override
    public void initialize(StrongPassword annotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null) {
            return false;
        }

        // Perform all validations
        boolean isValid = validateLength(password) &&
                validateUppercase(password) &&
                validateLowercase(password) &&
                validateDigit(password) &&
                validateSpecialChar(password) &&
                validateCommonPassword(password);

        if (!isValid) {
            log.warn("Password validation failed for security reasons");
        }

        return isValid;
    }

    /**
     * Validates minimum length requirement
     */
    private boolean validateLength(String password) {
        return password.length() >= MIN_LENGTH;
    }

    /**
     * Validates at least one uppercase letter
     */
    private boolean validateUppercase(String password) {
        return UPPERCASE.matcher(password).find();
    }

    /**
     * Validates at least one lowercase letter
     */
    private boolean validateLowercase(String password) {
        return LOWERCASE.matcher(password).find();
    }

    /**
     * Validates at least one digit
     */
    private boolean validateDigit(String password) {
        return DIGIT.matcher(password).find();
    }

    /**
     * Validates at least one special character
     */
    private boolean validateSpecialChar(String password) {
        return SPECIAL_CHAR.matcher(password).find();
    }

    /**
     * Validates password is not a common/weak password
     */
    private boolean validateCommonPassword(String password) {
        return !COMMON_PASSWORDS.contains(password.toLowerCase());
    }
}

