package com.vektortelekom.android.vservice.di.component

import com.vektortelekom.android.vservice.MainApp
import com.vektortelekom.android.vservice.di.module.ApplicationModule
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import javax.inject.Singleton

@Singleton
@Component(modules = [
    ApplicationModule::class
])
interface ApplicationComponent : AndroidInjector<MainApp> {

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun application(application: MainApp): Builder

        fun build(): ApplicationComponent
    }
}