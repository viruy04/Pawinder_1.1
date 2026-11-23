package com.example.pawinder_11.model;

public class Profile {
    private int id_user;
    private String nickname;
    private String email;
    private String name;
    private String surname;
    private Integer id_country;
    private String country;
    private Integer id_city;
    private String city;
    private Integer id_avatar;
    private String avatar; // имя файла аватарки из таблицы avatar

    public int getId() { return id_user; }
    public String getNickname() { return nickname; }
    public String getEmail() { return email; }
    public String getName() { return name; }
    public String getSurname() { return surname; }
    public Integer getIdCountry() { return id_country; }
    public String getCountry() { return country; }
    public Integer getIdCity() { return id_city; }
    public String getCity() { return city; }
    public Integer getIdAvatar() { return id_avatar; }
    public String getAvatar() { return avatar; }
}