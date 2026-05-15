package com.grullondev.firstapp.wear

import android.app.Application
import com.grullondev.firstapp.di.initKoin
import org.koin.android.ext.koin.androidContext

class WearApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@WearApp)
        }
    }
}
