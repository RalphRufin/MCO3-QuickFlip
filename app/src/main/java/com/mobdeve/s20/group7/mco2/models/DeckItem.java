package com.mobdeve.s20.group7.mco2.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.mobdeve.s20.group7.mco2.models.CardItem;

import java.util.ArrayList;

public class DeckItem implements Parcelable {
    private String id;  // Firestore document ID
    private String deckImage;
    private String deckTitle;
    private ArrayList<CardItem> cardItems;
    private boolean isPublic;

    // Default constructor (required for Firebase and manual initialization)
    public DeckItem() {
        this.id = "";
        this.deckImage = "";
        this.deckTitle = "";
        this.cardItems = new ArrayList<>();
        this.isPublic = false;
    }

    // Parameterized constructor
    public DeckItem(String id, String deckImage, String deckTitle, ArrayList<CardItem> cardItems, boolean isPublic) {
        this.id = id;
        this.deckImage = deckImage;
        this.deckTitle = deckTitle;
        this.cardItems = cardItems;
        this.isPublic = isPublic;
    }

    // Parcelable implementation
    protected DeckItem(Parcel in) {
        id = in.readString();
        deckImage = in.readString();
        deckTitle = in.readString();
        cardItems = in.createTypedArrayList(CardItem.CREATOR);
        isPublic = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(deckImage);
        dest.writeString(deckTitle);
        dest.writeTypedList(cardItems);
        dest.writeByte((byte) (isPublic ? 1 : 0));
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

    // Getters and setters for all fields
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public ArrayList<CardItem> getCardItems() {
        return cardItems;
    }

    public void setCardItems(ArrayList<CardItem> cardItems) {
        this.cardItems = cardItems;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }
}
