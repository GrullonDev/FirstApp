package com.grullondev.firstapp

import android.app.Application
import com.grullondev.firstapp.di.initKoin
import org.koin.android.ext.koin.androidContext

class FirstApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@FirstApp)
        }
    }
}
