package com.enviroclean.model

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by imobdev on 26/9/19
 */

data class TutorialModel(
    val strTitle: String,
    val strDesc: String,
    val imageId: Int
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel?, p1: Int) {
        parcel!!.writeString(strTitle)
        parcel.writeString(strDesc)
        parcel.writeInt(imageId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TutorialModel> {
        override fun createFromParcel(parcel: Parcel): TutorialModel {
            return TutorialModel(parcel)
        }

        override fun newArray(size: Int): Array<TutorialModel?> {
            return arrayOfNulls(size)
        }
    }
}