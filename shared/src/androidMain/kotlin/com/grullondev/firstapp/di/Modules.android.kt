package com.grullondev.firstapp.di

import com.grullondev.firstapp.data.local.getDatabaseBuilder
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single { getDatabaseBuilder(get()) }
}
