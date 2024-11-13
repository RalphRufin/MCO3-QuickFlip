package com.mobdeve.s20.group7.mco2.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DeckItem implements Serializable {
    private String deckImage;
    private String deckTitle;
    private List<CardItem> cardItems;

    // No-argument constructor required for Firestore and serialization
    public DeckItem() {
        this.deckImage = "";
        this.deckTitle = "";
        this.cardItems = new ArrayList<>();
    }

    public DeckItem(String deckImage, String deckTitle, List<CardItem> cardItems) {
        this.deckImage = deckImage;
        this.deckTitle = deckTitle;
        this.cardItems = cardItems;
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

    public void setDeckImage(String deckImage) {
        this.deckImage = deckImage;
    }

    public void setDeckTitle(String deckTitle) {
        this.deckTitle = deckTitle;
    }

    public void setCardItems(List<CardItem> cardItems) {
        this.cardItems = cardItems;
    }
}
