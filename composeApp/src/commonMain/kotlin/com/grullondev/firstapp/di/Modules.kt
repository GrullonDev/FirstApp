package com.grullondev.firstapp.di

import com.grullondev.firstapp.data.repository.InMemoryChatRepository
import com.grullondev.firstapp.data.repository.MockPermissionManager
import com.grullondev.firstapp.data.repository.PersistentSettingsRepository
import com.grullondev.firstapp.domain.repository.ChatRepository
import com.grullondev.firstapp.domain.repository.PermissionManager
import com.grullondev.firstapp.domain.repository.SettingsRepository
import com.grullondev.firstapp.presentation.viewmodel.CalendarViewModel
import com.grullondev.firstapp.presentation.viewmodel.ChatViewModel
import com.grullondev.firstapp.presentation.viewmodel.SettingsViewModel
import com.russhwolf.settings.Settings
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val dataModule = module {
    single { Settings() }
    singleOf(::InMemoryChatRepository) bind ChatRepository::class
    singleOf(::MockPermissionManager) bind PermissionManager::class
    singleOf(::PersistentSettingsRepository) bind SettingsRepository::class
}

val viewModelModule = module {
    viewModelOf(::ChatViewModel)
    viewModelOf(::SettingsViewModel)
    viewModelOf(::CalendarViewModel)
}

fun appModule() = listOf(dataModule, viewModelModule)
