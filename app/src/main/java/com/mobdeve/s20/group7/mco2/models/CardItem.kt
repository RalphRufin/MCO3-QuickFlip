package com.mobdeve.s20.group7.mco2.models

import android.os.Parcel
import android.os.Parcelable

data class CardItem(
    var question: String = "",  // Default values to prevent issues if Firestore doesn't provide them
    var answer: String = ""     // Default values to prevent issues if Firestore doesn't provide them
) : Parcelable {

    constructor(parcel: Parcel) : this(
        question = parcel.readString() ?: "",
        answer = parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(question)
        parcel.writeString(answer)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<CardItem> {
        override fun createFromParcel(parcel: Parcel): CardItem {
            return CardItem(parcel)
        }

        override fun newArray(size: Int): Array<CardItem?> {
            return arrayOfNulls(size)
        }
    }
}
