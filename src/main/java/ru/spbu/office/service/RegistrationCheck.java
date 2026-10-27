package ru.spbu.office.service;

public class RegistrationCheck {

    // Возвращает текст ошибки или null, если все хорошо
    public static String getError(String login, String password) {
        if (login == null || login.length() < 3 || login.length() > 50) {
            return "Логин должен быть от 3 до 50 символов";
        } else if (password == null || password.length() < 4) {
            return "Пароль должен быть не короче 4 символов";
        }
        return null;
    }
}
