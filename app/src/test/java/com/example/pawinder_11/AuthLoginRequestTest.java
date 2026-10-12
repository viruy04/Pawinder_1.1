package com.example.pawinder_11;

import com.example.pawinder_11.model.LoginRequest;
import com.google.gson.Gson;
import org.junit.Test;
import static org.junit.Assert.*;

public class AuthLoginRequestTest {

    @Test
    public void testLoginRequestSerialization() {
        LoginRequest request = new LoginRequest("test_user", "securePassword123");
        Gson gson = new Gson();
        String json = gson.toJson(request);

        // Проверяем, что запрос корректно сериализуется в JSON для отправки на сервер
        assertNotNull(json);
        assertTrue(json.contains("test_user"));
        assertTrue(json.contains("securePassword123"));
    }
}