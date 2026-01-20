package com.restaurant.system.restaurantmanagingsystem;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Order {
    private int id;
    private int tableNumber;
    private LocalDateTime orderTime;
    private String status;
    private double totalPrice;

    public Order(int id, int tableNumber, LocalDateTime orderTime, String status, double totalPrice) {
        this.id = id;
        this.tableNumber = tableNumber;
        this.orderTime = orderTime;
        this.status = status;
        this.totalPrice = totalPrice;
    }

    // Getters
    public int getId() { return id; }
    public int getTableNumber() { return tableNumber; }
    public LocalDateTime getOrderTime() { return orderTime; }
    public String getStatus() { return status; }
    public double getTotalPrice() { return totalPrice; }

    // Segédmetódus a szép dátumformátumhoz a táblázatban
    public String getFormattedTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return orderTime.format(formatter);
    }
}