package com.restaurant.system.restaurantmanagingsystem;

public class ActiveOrderItem {
    private int orderItemId; // Az adatbázis 'order_items' tábla ID-ja
    private String name;
    private int quantity;
    private double unitPrice;

    public ActiveOrderItem(int orderItemId, String name, int quantity, double unitPrice) {
        this.orderItemId = orderItemId;
        this.name = name;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public int getOrderItemId() { return orderItemId; }
    public String getName() { return name; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public double getTotalPrice() { return quantity * unitPrice; }

    @Override
    public String toString() {
        return name + " - " + quantity + " db - " + (int)getTotalPrice() + " Ft";
    }
}
