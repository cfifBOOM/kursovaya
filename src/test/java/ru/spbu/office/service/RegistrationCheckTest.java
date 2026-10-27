package ru.spbu.office.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class RegistrationCheckTest {

    @Test
    void testCorrect() {
        assertNull(RegistrationCheck.getError("ivan", "1234"));
        assertNull(RegistrationCheck.getError("admin", "password"));
    }

    @Test
    void testBoundaries() {
        // Логин: от 3 до 50 символов
        assertNull(RegistrationCheck.getError("abc", "1234"));
        assertNull(RegistrationCheck.getError("a".repeat(50), "1234"));
        assertEquals("Логин должен быть от 3 до 50 символов", RegistrationCheck.getError("ab", "1234"));
        assertEquals("Логин должен быть от 3 до 50 символов", RegistrationCheck.getError("a".repeat(51), "1234"));
        assertEquals("Логин должен быть от 3 до 50 символов", RegistrationCheck.getError("", "1234"));

        // Пароль: не короче 4 символов
        assertNull(RegistrationCheck.getError("ivan", "1234"));
        assertEquals("Пароль должен быть не короче 4 символов", RegistrationCheck.getError("ivan", "123"));
        assertEquals("Пароль должен быть не короче 4 символов", RegistrationCheck.getError("ivan", ""));
    }
}
