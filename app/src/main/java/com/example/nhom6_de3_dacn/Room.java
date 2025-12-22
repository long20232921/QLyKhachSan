package com.example.nhom6_de3_dacn;

import java.io.Serializable;

public class Room implements Serializable {
    private String id;
    private String name;
    private String price;
    private String image;
    private String description;
    private double rating;
    private int maxGuests;
    private String status;

    // Constructor rỗng
    public Room() { }

    public Room(String name, String price, String image, int maxGuests) {
        this.name = name;
        this.price = price;
        this.image = image;
        this.maxGuests = maxGuests;
    }

    // --- GETTER & SETTER ---

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public int getMaxGuests() { return maxGuests; }
    public void setMaxGuests(int maxGuests) { this.maxGuests = maxGuests; }

    public String getStatus() { return status != null ? status : "AVAILABLE"; }
    public void setStatus(String status) { this.status = status; }
}