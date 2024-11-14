package com.mobdeve.s20.group7.mco2.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class DeckItem implements Parcelable {
    private String deckImage;
    private String deckTitle;
    private ArrayList<CardItem> cardItems;

    // No-argument constructor for Firestore and default instantiation
    public DeckItem() {
        this.deckImage = "";
        this.deckTitle = "";
        this.cardItems = new ArrayList<>();
    }

    public DeckItem(String deckImage, String deckTitle, ArrayList<CardItem> cardItems) {
        this.deckImage = deckImage;
        this.deckTitle = deckTitle;
        this.cardItems = cardItems;
    }

    protected DeckItem(Parcel in) {
        deckImage = in.readString();
        deckTitle = in.readString();
        cardItems = in.createTypedArrayList(CardItem.CREATOR); // Reading the CardItem list
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(deckImage);
        dest.writeString(deckTitle);
        dest.writeTypedList(cardItems); // Writing the CardItem list
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<DeckItem> CREATOR = new Creator<DeckItem>() {
        @Override
        public DeckItem createFromParcel(Parcel in) {
            return new DeckItem(in);
        }

        @Override
        public DeckItem[] newArray(int size) {
            return new DeckItem[size];
        }
    };

    public String getDeckImage() {
        return deckImage;
    }

    public void setDeckImage(String deckImage) {
        this.deckImage = deckImage;
    }

    public String getDeckTitle() {
        return deckTitle;
    }

    public void setDeckTitle(String deckTitle) {
        this.deckTitle = deckTitle;
    }

    public List<CardItem> getCardItems() {
        return cardItems;
    }

    public void setCardItems(ArrayList<CardItem> cardItems) {
        this.cardItems = cardItems;
    }
}
