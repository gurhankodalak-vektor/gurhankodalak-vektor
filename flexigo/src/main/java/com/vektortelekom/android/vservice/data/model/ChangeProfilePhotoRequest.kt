package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ChangeProfilePhotoRequest(
        @SerializedName("profileImageUuid")
        val profileImageUuid: String
) : Serializable