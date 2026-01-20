package com.restaurant.system.restaurantmanagingsystem;

public class BasketItem {
    private MenuItem item;
    private int quantity;

    public BasketItem(MenuItem item, int quantity) {
        this.item = item;
        this.quantity = quantity;
    }

    public MenuItem getItem() { return item; }
    public int getQuantity() { return quantity; }
    public void addOne() { this.quantity++; }

    @Override
    public String toString() {
        return item.getName() + " x" + quantity + " (" + (item.getPrice() * quantity) + " Ft)";
    }
}