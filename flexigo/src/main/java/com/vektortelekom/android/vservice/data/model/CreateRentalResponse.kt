package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName
import com.vektor.vshare_api_ktx.model.DeliveryAddressModel2
import com.vektor.vshare_api_ktx.model.PriceModel2
import java.io.Serializable

data class CreateRentalResponse (
        @SerializedName("userInfo")
        val userInfo: UserInfoModel,
        @SerializedName("rental")
        val rental: RentalModel,
        @SerializedName("deliveryAddress")
        val deliveryAddress: DeliveryAddressModel2,
        @SerializedName("customerAddress")
        val customerAddress: DeliveryAddressModel2,
        @SerializedName("initialVehicleAddress")
        val initialVehicleAddress: DeliveryAddressModel2,
        @SerializedName("vehicle")
        val vehicle: PoolCarVehicleModel,
        @SerializedName("priceModel2")
        val priceModel2: PriceModel2
): Serializable