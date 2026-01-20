package com.restaurant.system.restaurantmanagingsystem;

public class Category {
    private int id;
    private String name;
    private Integer parentId; // Integer, mert lehet null

    public Category(int id, String name, Integer parentId) {
        this.id = id;
        this.name = name;
        this.parentId = parentId;
    }

    // Getterek és Setterek
    public int getId() { return id; }
    public String getName() { return name; }
    public Integer getParentId() { return parentId; }

    @Override
    public String toString() {
        // Ha van szülője, jelezzük a névben (pl. "Sör (Alkohol)" vagy "Cola (Üdítő)")
        // Ehhez szükség lehet a szülő nevének lekérésére is, de kezdésnek ez is jó:
        return (parentId != null ? "  ↳ " : "") + name;
    }
}