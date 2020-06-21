package ru.vkr.vkr.entity.api;

import ru.vkr.vkr.entity.User;

/**
 * регистрационные данные пользователя
 */
public interface PersonRegisterData {
    // пользователь
    User getUser();

    // имя пользователя
    String getName();

    // фамилия пользователя
    String getSurname();

    // отчество пользователя
    String getMiddleName();
}
