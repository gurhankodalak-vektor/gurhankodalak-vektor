package com.vektortelekom.android.vservice.data.model
import com.google.gson.annotations.SerializedName
import com.vektor.ktx.data.remote.usermanagement.model.BaseResponse
import java.io.Serializable

class CountryCodeResponse (
        @SerializedName("en")
        val en: List<CountryCodeResponseListModel>,
        @SerializedName("tr")
        val tr: List<CountryCodeResponseListModel>
) : Serializable, BaseResponse()

class CountryCodeResponseListModel (
        @SerializedName("shortCode")
        val shortCode: String,
        @SerializedName("name")
        val name: String,
        @SerializedName("areaCode")
        val areaCode: String
) : Serializable