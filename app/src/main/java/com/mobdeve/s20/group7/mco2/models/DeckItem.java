package com.mobdeve.s20.group7.mco2.models;

import java.util.ArrayList;
import java.util.List;

public class DeckItem {

    private String deckImage;
    private String deckTitle;
    private List<CardItem> cardItems;

    // No-argument constructor required for Firestore
    public DeckItem() {
        this.deckImage = "";
        this.deckTitle = "";
        this.cardItems = new ArrayList<>();
    }

    // Constructor with arguments
    public DeckItem(String deckImage, String deckTitle) {
        this.deckImage = deckImage;
        this.deckTitle = deckTitle;
        this.cardItems = new ArrayList<>();
    }

    public void addCardItem(CardItem cardItem) {
        cardItems.add(cardItem);
    }

    public String getDeckImage() {
        return deckImage;
    }

    public String getDeckTitle() {
        return deckTitle;
    }

    public List<CardItem> getCardItems() {
        return cardItems;
    }

    public CardItem getCardItem(int index) {
        if (index >= 0 && index < cardItems.size()) {
            return cardItems.get(index);
        }
        return null;
    }
}
