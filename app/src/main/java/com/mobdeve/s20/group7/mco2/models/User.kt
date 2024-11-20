package com.mobdeve.s20.group7.mco2.models

import android.os.Parcel
import android.os.Parcelable

class User(
    var username: String = "",
    var email: String = "",
    var profilePicUrl: String = "",
    var authMethod: String = "",
    var deckItems: ArrayList<DeckItem> = ArrayList(),
    var points: Int = 0  // Added points field
) : Parcelable {

    constructor(parcel: Parcel) : this(
        username = parcel.readString() ?: "",
        email = parcel.readString() ?: "",
        profilePicUrl = parcel.readString() ?: "",
        authMethod = parcel.readString() ?: "",
        deckItems = parcel.createTypedArrayList(DeckItem.CREATOR) ?: ArrayList(),
        points = parcel.readInt()  // Read points from parcel
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(username)
        parcel.writeString(email)
        parcel.writeString(profilePicUrl)
        parcel.writeString(authMethod)
        parcel.writeTypedList(deckItems)
        parcel.writeInt(points)  // Write points to parcel
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): User = User(parcel)
        override fun newArray(size: Int): Array<User?> = arrayOfNulls(size)
    }
}
