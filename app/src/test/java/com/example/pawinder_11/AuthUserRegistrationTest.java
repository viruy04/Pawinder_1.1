package com.example.pawinder_11;

import com.example.pawinder_11.model.User;
import com.google.gson.Gson;
import org.junit.Test;
import static org.junit.Assert.*;

public class AuthUserRegistrationTest {

    //Проверяет корректность обработки данных пользователя
    // после успешной регистрации или получения ответа от сервера
    @Test
    public void testUserRegistrationParsing() {
        String json = "{\"id_user\":7, \"nickname\":\"new_pet_lover\", \"email\":\"lover@pawinder.com\"}";
        Gson gson = new Gson();
        User user = gson.fromJson(json, User.class);

        assertNotNull(user);
        assertEquals(7, user.getId());
        assertEquals("new_pet_lover", user.getNickname());
        assertEquals("lover@pawinder.com", user.getEmail());
    }
}