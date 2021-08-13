package com.example.jgi.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

sealed class ResponseObject

@Parcelize
open class BaseResposne(
    val code: Int = 0,
    val errorCode: ArrayList<Int> = ArrayList(),
    val errorMessage: ArrayList<String> = ArrayList(),
    val responseMessage: String? = null,
    val httpStatus: Int = 0
) : ResponseObject(), Parcelable


@Parcelize
data class User(
    val userId: String,
    val name: String?,
    val email: String?,
    val phonenumber: String?,
    val type: String?,
    val comments:String?,
    val versionNumber:String?,
    val url:String

) : Parcelable {
    constructor() : this("", "", "", "", "","","","")
}

//@Parcelize
//data class GitCardResponse(
//    @SerializedName("gift") val giftcard: GiftCard?
//) :  Parcelable
//
//
//@Parcelize
//data class GetGitCardResponde(
//    @SerializedName("d") val giftcard: GiftCard?
//) : BaseResposne(), Parcelable
//

@Parcelize
data class Event(
    val eventName: String,
    val eventDescription: String,
    val eventBy: String,
    val totalAmount: String?,
    val amountCollected: String,
    val percentage: String?,
    val url: String
) : Parcelable {
    constructor() : this("", "", "", "", "", "", "")
}
