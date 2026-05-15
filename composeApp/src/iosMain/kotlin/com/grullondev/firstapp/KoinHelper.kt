package com.grullondev.firstapp

import com.grullondev.firstapp.di.initKoin
import com.grullondev.firstapp.di.mobileViewModelModule

fun initKoin() {
    initKoin(additionalModules = listOf(mobileViewModelModule)) { }
}
