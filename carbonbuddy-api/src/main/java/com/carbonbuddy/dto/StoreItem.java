package com.carbonbuddy.dto;

/**
 * Data Transfer Object representing an item in the reward store.
 * Includes affordability status based on the user's current balance.
 */
public class StoreItem {

    private int id;
    private String name;
    private String description;
    private int cost;
    private String icon;
    private boolean affordable;

    /**
     * Default constructor.
     */
    public StoreItem() {}

    /**
     * Constructs a StoreItem with all required fields.
     *
     * @param id          the item ID
     * @param name        the item name
     * @param description the item description
     * @param cost        the cost in CarbonCoins
     * @param icon        the item icon identifier
     */
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
