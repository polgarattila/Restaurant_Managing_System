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
        if (parentId != null && parentId > 0) {
            return "    ↳ " + name; // Négy szóköz és egy nyíl az alkategóriáknak
        }
        return name.toUpperCase(); // A főkategóriák legyenek nagybetűsek a hangsúly kedvéért
    }
}