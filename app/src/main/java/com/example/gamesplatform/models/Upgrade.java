package com.example.gamesplatform.models;

public class Upgrade {
    private String name;
    private String description;
    private int price;
    private boolean isPurchased;

    public Upgrade(String name, String description, int price) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.isPurchased = false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public boolean isPurchased() {
        return isPurchased;
    }

    public void setPurchased(boolean purchased) {
        isPurchased = purchased;
    }
}
