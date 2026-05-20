package com.grullondev.firstapp.di

import com.grullondev.firstapp.data.local.*
import com.grullondev.firstapp.data.repository.*
import com.grullondev.firstapp.domain.repository.*
import com.russhwolf.settings.Settings
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val dataModule = module {
    single { Settings() }
    single { getRoomDatabase(get()) }
    singleOf(::FirebaseChatRepository) bind ChatRepository::class
    singleOf(::RoomCalendarRepository) bind CalendarRepository::class
    singleOf(::MockPermissionManager) bind PermissionManager::class
    singleOf(::PersistentSettingsRepository) bind SettingsRepository::class
}

expect val platformModule: Module

fun appModule() = listOf(dataModule, platformModule)
