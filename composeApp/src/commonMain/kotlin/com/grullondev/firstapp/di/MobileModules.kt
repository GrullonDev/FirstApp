package com.grullondev.firstapp.di

import com.grullondev.firstapp.presentation.viewmodel.CalendarViewModel
import com.grullondev.firstapp.presentation.viewmodel.ChatViewModel
import com.grullondev.firstapp.presentation.viewmodel.SettingsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val mobileViewModelModule = module {
    viewModelOf(::ChatViewModel)
    viewModelOf(::SettingsViewModel)
    viewModelOf(::CalendarViewModel)
}
