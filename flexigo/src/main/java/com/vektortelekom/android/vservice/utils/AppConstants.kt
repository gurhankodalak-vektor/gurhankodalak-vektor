package com.vektortelekom.android.vservice.utils

import com.vektortelekom.android.vservice.BuildConfig

class AppConstants {

    object System {
        val APP_NAME = if(BuildConfig.FLAVOR == "tums") "FLEXIGO_TUMS" else "FLEXIGO"
        const val APP_NAME_STD = "FlexiGo"
        const val PREF_STATE_FILE_NAME = "fg_def_sc_pref"
        const val PREF_DATA_FILE_NAME = "fg_data_sc_pref"

        const val CALL_CENTER_NUMBER = "02122513555"
    }

    object Documents {
        const val CONFIDENTIALITY_AGREEMENT = "https://secure.vektorteknoloji.com/docs/flexigo/kvkk.pdf"
        const val KVKK_TUMS = "https://secure.vektorteknoloji.com/docs/flexigo/kvkkTums.pdf"
        const val MEMBERSHIP_AGREEMENT = "https://secure.vektorteknoloji.com/docs/flexigo/kvkk.pdf"
        const val GRDP_AGREEMENT = "https://secure.vektorteknoloji.com/docs/flexigo/kvkk.pdf"
        val CAMPAIGN_AGREEMENT = "https://secure.vektorteknoloji.com/docs/flexigo/kvkk.pdf"
        const val EXPENSE_POLICY = "https://secure.vektorteknoloji.com/docs/flexigo/kvkk.pdf"
    }

    object RatingType {
        const val OVERALL = "OVERALL"
        const val CLEANLINESS = "CLEANLINESS"
        const val VEHICLE = "VEHICLE"
    }

    object RatingType2 {
        const val OVERALL = "OVERALL"
        const val DRIVER = "DRIVER"
        const val VEHICLE = "VEHICLE"
    }

    object DocumentType {
        //val SELFIE = "SELFIE"
        const val DRIVING_LICENSE = "DRIVING_LICENSE"
        //val IDENTITY = "IDENTITY"
        //val PASSPORT = "PASSPORT"
        //val DIGITAL_AGREEMENT = "DIGITAL_AGREEMENT"
    }

}