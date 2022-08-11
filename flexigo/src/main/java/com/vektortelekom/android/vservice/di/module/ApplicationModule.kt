package com.vektortelekom.android.vservice.di.module

import android.app.Application
import android.content.Context
import com.vektor.ktx.di.module.NetworkModule

import com.vektortelekom.android.vservice.MainApp
import com.vektortelekom.android.vservice.di.builders.ActivityBuilderModule
import dagger.Binds
import dagger.Module
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Module(includes = [
    AndroidSupportInjectionModule::class,
    SystemModule::class,
    NetworkModule::class,
    ServiceModule::class,
    RepositoryModule::class,
    ActivityBuilderModule::class
])
abstract class ApplicationModule {

    @Binds
    @Singleton
    abstract fun bindApplication(application: MainApp): Application

    @Binds
    @Singleton
    abstract fun bindApplicationContext(application: Application): Context
}