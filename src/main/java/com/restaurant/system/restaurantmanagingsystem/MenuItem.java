package com.restaurant.system.restaurantmanagingsystem;

public class MenuItem {
    private int id;
    private String name;
    private double price;
    private String categoryName;

    public MenuItem(int id, String name, double price, String categoryName) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.categoryName = categoryName;
    }

    // Getterek (ezek kellenek majd a JavaFX táblázathoz!)
    public int getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public String getCategoryName() { return categoryName; }
}