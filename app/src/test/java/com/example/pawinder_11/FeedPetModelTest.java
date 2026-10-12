package com.example.pawinder_11;

import com.example.pawinder_11.model.Pet;
import com.google.gson.Gson;
import org.junit.Test;
import static org.junit.Assert.*;

public class FeedPetModelTest {

    //Проверяет, что данные карточки питомца
    // (имя, описание, ID и ссылка на фото),
    // поступающие с сервера в формате JSON,
    // корректно преобразуются (десериализуются)
    // в объект Pet, используемый в ленте.
    @Test
    public void testPetCardParsing() {
        String json = "{\"id\":42, \"name\":\"Рекс\", \"description\":\"Энергичный щенок\", \"photoUrl\":\"/images/rex.jpg\"}";
        Gson gson = new Gson();
        Pet pet = gson.fromJson(json, Pet.class);

        assertNotNull(pet);
        assertEquals(42, pet.getId());
        assertEquals("Рекс", pet.getName());
        assertEquals("Энергичный щенок", pet.getDescription());
        assertEquals("/images/rex.jpg", pet.getPhotoUrl());
    }
}