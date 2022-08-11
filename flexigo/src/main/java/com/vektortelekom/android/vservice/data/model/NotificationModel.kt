package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class NotificationModel (
        @SerializedName("id")
        val id: Int?,
        @SerializedName("category")
        val category: String,
        @SerializedName("subCategory")
        val subCategory: String,
        @SerializedName("creationTime")
        val creationTime: Long,
        @SerializedName("notificationDay")
        val notificationDay: Int,
        @SerializedName("name")
        val name: String?,
        @SerializedName("title")
        val title: String,
        @SerializedName("message")
        val message: String,
        @SerializedName("date")
        var date: Long?,
        @SerializedName("fileName")
        val fileName: String?,
        /*@SerializedName("mapAdditionalParameters")
        val mapAdditionalParameters: JsonObject?,*/
        @SerializedName("relatedId")
        val relatedId: Int?,
        @SerializedName("companyId")
        val companyId: Int?,
        @SerializedName("routeId")
        val routeId: Int?,
        @SerializedName("destinationName")
        val destinationName: String?,
        @SerializedName("internalKey")
        val internalKey: String?,
        @SerializedName("vehicleId")
        val vehicleId: Int?,
        @SerializedName("operation")
        val operation: String?,
        @SerializedName("fileId")
        val fileId: Int?
) : Serializable