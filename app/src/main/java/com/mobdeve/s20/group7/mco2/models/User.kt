package com.mobdeve.s20.group7.mco2.models

import android.os.Parcel
import android.os.Parcelable

class User(
    var username: String = "",
    var email: String = "",
    var profilePicUrl: String = "",
    var authMethod: String = "",
    var deckItems: ArrayList<DeckItem> = ArrayList()
) : Parcelable {

    constructor(parcel: Parcel) : this(
        username = parcel.readString() ?: "",
        email = parcel.readString() ?: "",
        profilePicUrl = parcel.readString() ?: "",
        authMethod = parcel.readString() ?: "",
        deckItems = parcel.createTypedArrayList(DeckItem.CREATOR) ?: ArrayList()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(username)
        parcel.writeString(email)
        parcel.writeString(profilePicUrl)
        parcel.writeString(authMethod)
        parcel.writeTypedList(deckItems)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): User = User(parcel)

        override fun newArray(size: Int): Array<User?> = arrayOfNulls(size)
    }
}
