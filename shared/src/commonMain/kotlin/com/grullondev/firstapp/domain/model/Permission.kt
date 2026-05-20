package com.grullondev.firstapp.domain.model

enum class Permission {
    CAMERA,
    GALLERY,
    RECORD_AUDIO,
    FILE_STORAGE
}

enum class PermissionState {
    GRANTED,
    DENIED,
    NOT_DETERMINED
}
