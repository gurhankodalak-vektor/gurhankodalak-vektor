package com.vektortelekom.android.vservice.utils

import android.content.Context
import android.util.Log
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.annotation.GlideModule

import com.bumptech.glide.module.AppGlideModule
import com.vektortelekom.android.vservice.BuildConfig

@GlideModule
class DefaultGlideModule : AppGlideModule() {
    override fun applyOptions(context: Context, builder: GlideBuilder) {
        if (BuildConfig.DEBUG)
            builder.setLogLevel(Log.VERBOSE)
    }
}