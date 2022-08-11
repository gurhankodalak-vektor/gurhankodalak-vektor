package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName
import com.vektor.vshare_api_ktx.model.DeliveryAddressModel2
import java.io.Serializable

data class StationVehicleModel(
        @SerializedName("id")
        val id: Int,
        @SerializedName("availableCount")
        val availableCount: Int,
        @SerializedName("vehicle")
        val vehicle: PoolCarVehicleModel,
        @SerializedName("imageUuid")
        val imageUuid: String?
) : Serializable

class PoolCarVehicleModel(
    @SerializedName("id")
        val id: Int,
    @SerializedName("make")
        val make: String,
    @SerializedName("model")
        val model: String,
    @SerializedName("description")
        val description: String,
    @SerializedName("fuelType")
        val fuelType: String?,
    @SerializedName("transmissionType")
        val transmissionType: String?,
    @SerializedName("bodyType")
        val bodyType: BodyType,
    @SerializedName("imageName")
        val imageName: String?,
    @SerializedName("modelYear")
        val modelYear: Int,
    @SerializedName("priceModelId")
        val priceModelId: Int? = null,
    @SerializedName("location")
        val location: DeliveryAddressModel2?,
    @SerializedName("plate")
        val plate: String,
    @SerializedName("chassisNumber")
        val chassisNumber: String,
    @SerializedName("deviceType")
        val deviceTypeModel: DeviceType?
) : Serializable {
    var deviceType: DeviceType
        get() {
            return deviceTypeModel ?: DeviceType.REMOTE_DOOR
        }
        set(value) {

        }
}

enum class FuelType {
    dizel,
    benzinli
}

enum class BodyType {
    sedan,
    hatchback,
    vagon
}

enum class TransmissionType {
    manuel,
    otomatik
}

enum class DeviceType {
    @SerializedName("BASIC_TELEMATIC")
    BASIC_TELEMATIC,
    @SerializedName("REMOTE_DOOR")
    REMOTE_DOOR,
    @SerializedName("NONE")
    NONE
}