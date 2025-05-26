package com.example.librarychecker.models;

import java.util.List;

public class User {
    private String email;
    private String name;
    private String surname;
    private List<String> reservedBooks;

    public User() {}

    public User(String email, String name, String surname, List<String> reservedBooks) {
        this.email = email;
        this.name = name;
        this.surname = surname;
        this.reservedBooks = reservedBooks;
    }

    // --- геттеры и сеттеры ---
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }

    public List<String> getReservedBooks() { return reservedBooks; }
    public void setReservedBooks(List<String> reservedBooks) { this.reservedBooks = reservedBooks; }
}
