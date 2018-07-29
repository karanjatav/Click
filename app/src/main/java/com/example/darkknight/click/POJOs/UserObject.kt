package com.example.darkknight.click.POJOs

import android.os.Parcel
import android.os.Parcelable

class UserObject(var userId: Int, var profile_pic: String, var name: String, var email: String, var socialId: String, var password: String) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(userId)
        parcel.writeString(profile_pic)
        parcel.writeString(name)
        parcel.writeString(email)
        parcel.writeString(socialId)
        parcel.writeString(password)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<UserObject> {
        override fun createFromParcel(parcel: Parcel): UserObject {
            return UserObject(parcel)
        }

        override fun newArray(size: Int): Array<UserObject?> {
            return arrayOfNulls(size)
        }
    }
}