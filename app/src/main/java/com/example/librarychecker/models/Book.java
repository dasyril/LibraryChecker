package com.example.librarychecker.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Book implements Serializable {
    private String id;
    private String title;
    private String author;
    private String genre;
    private String description;
    private String imageUrl;
    private int count;
    private List<String> reservedBy = new ArrayList<>();            // UID пользователей
    private List<String> reservedUntil;         // Дата возврата (в формате yyyy-MM-dd)
    private List<String> reservationDates;      // Дата бронирования

    public Book() {}

    public Book(String id, String title, String author, String genre, String description, String imageUrl,
                int count, List<String> reservedBy, List<String> reservedUntil, List<String> reservationDates) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.description = description;
        this.imageUrl = imageUrl;
        this.count = count;
        this.reservedBy = reservedBy;
        this.reservedUntil = reservedUntil;
        this.reservationDates = reservationDates;
    }

    // Геттеры и сеттеры
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }

    public List<String> getReservedBy() {
        if (reservedBy == null) reservedBy = new ArrayList<>();
        return reservedBy;
    }

    public List<String> getReservedUntil() {
        if (reservedUntil == null) reservedUntil = new ArrayList<>();
        return reservedUntil;
    }

    public List<String> getReservationDates() {
        if (reservationDates == null) reservationDates = new ArrayList<>();
        return reservationDates;
    }

    public void setReservedBy(List<String> reservedBy) {
        this.reservedBy = reservedBy;
    }

    public void setReservedUntil(List<String> reservedUntil) {
        this.reservedUntil = reservedUntil;
    }

    public void setReservationDates(List<String> reservationDates) {
        this.reservationDates = reservationDates;
    }
}