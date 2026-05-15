package com.grullondev.firstapp.di

import com.grullondev.firstapp.data.local.*
import com.grullondev.firstapp.data.repository.*
import com.grullondev.firstapp.domain.repository.*
import com.grullondev.firstapp.presentation.viewmodel.*
import com.russhwolf.settings.Settings
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val dataModule = module {
    single { Settings() }
    single { getRoomDatabase(get()) }
    singleOf(::RoomChatRepository) bind ChatRepository::class
    singleOf(::RoomCalendarRepository) bind CalendarRepository::class
    singleOf(::MockPermissionManager) bind PermissionManager::class
    singleOf(::PersistentSettingsRepository) bind SettingsRepository::class
}

val viewModelModule = module {
    viewModelOf(::ChatViewModel)
    viewModelOf(::SettingsViewModel)
    viewModelOf(::CalendarViewModel)
}

expect val platformModule: Module

fun appModule() = listOf(dataModule, viewModelModule, platformModule)
