package com.carbonbuddy.dto;

public class StoreItem {
    private int id;
    private String name;
    private String description;
    private int cost;
    private String icon;
    private boolean affordable;

    public StoreItem() {}

    public StoreItem(int id, String name, String description, int cost, String icon) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.cost = cost;
        this.icon = icon;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getCost() { return cost; }
    public void setCost(int cost) { this.cost = cost; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public boolean isAffordable() { return affordable; }
    public void setAffordable(boolean affordable) { this.affordable = affordable; }
}
