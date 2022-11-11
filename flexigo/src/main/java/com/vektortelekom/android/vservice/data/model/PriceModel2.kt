package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class PriceModel2 : Serializable {

    enum class CarType(val carName: String) {
        PROMO("PROMO"),
        BASIC("BASIC"),
        @SerializedName("BASIC+")
        BASICPLUS("BASIC+"),
        EASY("EASY"),
        @SerializedName("EASY+")
        EASYPLUS("EASY+"),
        CITY("CITY"),
        @SerializedName("CITY+")
        CITYPLUS("CITY+"),
        COMFORT("COMFORT"),
        @SerializedName("COMFORT+")
        COMFORTPLUS("COMFORT+"),
        COOL("COOL"),
        FUN("FUN"),
        CARPOOL("CARPOOL"),
        OTHER("OTHER")
    }

    @SerializedName("application")
    var application: String? = null

    @SerializedName("kmExceededCost")
    var kmExceededCost: Double? = null

    @SerializedName("startingCost")
    var startingCost: Double? = null

    @SerializedName("hourlyCost")
    var hourlyCost: Double? = null

    @SerializedName("hourlyCostBeforeDiscount")
    var hourlyCostBeforeDiscount: Double? = null

    @SerializedName("provisionCost")
    var provisionCost: Double? = null

    @SerializedName("kmPerRentalLimit")
    var kmPerRentalLimit: Double? = null

    @SerializedName("kmPerHourLimit")
    var kmPerHourLimit: Double? = null

    @SerializedName("kmPerDayLimit")
    var kmPerDayLimit: Double? = null

    @SerializedName("dailyCost")
    var dailyCost: Double? = null

    @SerializedName("name")
    var name: CarType? = null
        get() {
            if (field == null)
                return CarType.OTHER
            return field
        }

    @SerializedName("description")
    var description: String? = null

    @SerializedName("currency")
    var currency: String? = null

    @SerializedName("id")
    var id: Int? = null

    @SerializedName("pricingType")
    var pricingType: String? = null

    @SerializedName("chargingIntervalMinutes")
    var chargingIntervalMinutes: Double? = null

    @SerializedName("minimumChargingMinutes")
    var minimumChargingMinutes: Double? = null

    @SerializedName("convertToDailyThreshold")
    var convertToDailyThreshold: Double? = null

}