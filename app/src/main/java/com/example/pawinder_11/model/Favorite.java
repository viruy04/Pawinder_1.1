package com.example.pawinder_11.model;

public class Favorite {
    private int id_favorite;
    private Pet pet; // если сервер отдаёт питомца вложенным объектом

    public int getId() { return id_favorite; }
    public Pet getPet() { return pet; }
}
