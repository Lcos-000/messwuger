package com.campusassistant.android.data.model

import com.google.gson.annotations.SerializedName

data class PersonalizationProfile(
    val studentId: String?,
    val avatar: String?,
    val background: String?,
    val wallpaper: String?,
    val cardOpacity: Double?,
    val cardBlur: Double?,
    val globalFontEnabled: Int?,
    val wallpaperMask: Double?
)

data class PersonalizationUpdateRequest(
    val avatar: String,
    val background: String,
    val wallpaper: String,
    val cardOpacity: Double,
    val cardBlur: Double,
    val globalFontEnabled: Int,
    val wallpaperMask: Double
)

data class DefaultAssetOptions(
    val avatars: List<String>?,
    val backgrounds: List<String>?,
    val wallpapers: List<String>?
)

data class CustomAssets(
    @SerializedName(value = "customAvatar", alternate = ["avatar"])
    val customAvatar: String?,
    @SerializedName(value = "customBackground", alternate = ["background"])
    val customBackground: String?,
    @SerializedName(value = "customWallpaper", alternate = ["wallpaper"])
    val customWallpaper: String?
)

data class UploadedAsset(
    val type: String?,
    val url: String?
)
