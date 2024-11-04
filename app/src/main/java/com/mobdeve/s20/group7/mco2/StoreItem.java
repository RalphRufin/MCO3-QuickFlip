package com.mobdeve.s20.group7.mco2;

public class StoreItem {
    private final String name;
    private final int pointsCost;
    private final int iconResource;

    public StoreItem(String name, int pointsCost, int iconResource) {
        this.name = name;
        this.pointsCost = pointsCost;
        this.iconResource = iconResource;
    }

    public String getName() {
        return name;
    }

    public int getPointsCost() {
        return pointsCost;
    }

    public int getIconResource() {
        return iconResource;
    }
}

