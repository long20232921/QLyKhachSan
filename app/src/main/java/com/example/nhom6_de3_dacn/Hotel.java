package com.example.nhom6_de3_dacn;

public class Hotel {
    private String name;
    private String location;
    private double rating;
    private String price;
    private int imageResId;

    public Hotel(String name, String location, double rating, String price, int imageResId) {
        this.name = name;
        this.location = location;
        this.rating = rating;
        this.price = price;
        this.imageResId = imageResId;
    }

    public String getName() { return name; }
    public String getLocation() { return location; }
    public double getRating() { return rating; }
    public String getPrice() { return price; }
    public int getImageResId() { return imageResId; }
}