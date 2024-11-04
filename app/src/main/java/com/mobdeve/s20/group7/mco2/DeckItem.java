package com.mobdeve.s20.group7.mco2;

import java.util.ArrayList;
import java.util.List;

public class DeckItem {

    private final String deckImage;
    private final String deckTitle;
    private final List<CardItem> cardItems;

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
