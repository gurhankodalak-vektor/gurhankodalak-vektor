package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName

data class UserInfoModel(@SerializedName("id")
                         val id: Int? = null,
                         @SerializedName("fullName")
                         val fullName: String? = null,
                         @SerializedName("surname")
                         val surname: String? = null,
                         @SerializedName("name")
                         val name: String? = null,
                         @SerializedName("mobile")
                         val mobile: String? = null,
                         @SerializedName("token")
                         val token: String? = null,
                         @SerializedName("email")
                         val email: String? = null,
                         @SerializedName("birthdate")
                         val birthdate: String? = null,
                         @SerializedName("tckn")
                         val tckn: String? = null,
                         @SerializedName("licenseNumber")
                         val licenseNumber: String?,
                         @SerializedName("driverLicenceId")
                         val driverLicenceId: String?,
                         @SerializedName("driverLicenceIssueDate")
                         val driverLicenceIssueDate: String? = null,
                         @SerializedName("profileImageUuid")
                         val profileImageUuid: String? = null,
                         @SerializedName("driverLicenceImageUuid")
                         val driverLicenceImageUuid: String? = null,
                         @SerializedName("driverLicenceImageUuid2")
                         val driverLicenceImageUuid2: String? = null,
                         @SerializedName("passportImageUuid")
                         val passportImageUuid: String? = null,
                         @SerializedName("signedAggrement")
                         val signedAgreement: Boolean? = null,
                         @SerializedName("canStartRental")
                         val canStartRental: Boolean? = null,
                         @SerializedName("nationality")
                         val nationality: String? = null,
                         @SerializedName("countryEntryDate")
                         val countryEntryDate: String? = null,
                         @SerializedName("preferredLanguage")
                         val preferredLanguage: String? = null,
                         @SerializedName("mobileCountryCode")
                         val mobileCountryCode: String? = null,
                         @SerializedName("corporateOrganizationId")
                         val corporateOrganizationId: Int? = null,
                         @SerializedName("firstRentalDate")
                         val firstRentalDate: String? = null,
                         @SerializedName("application")
                         val application: String? = null,
                         @SerializedName("isIdentityEntered")
                         val isIdentityEntered: Boolean? = null,
                         @SerializedName("accountId")
                         val accountId: Int

)