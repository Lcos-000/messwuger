package com.campusassistant.android.ui.profile

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.campusassistant.android.core.network.ServerConfigHolder
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URI

internal fun assetOptions(fallbackLabel: String, defaultPrefix: String, defaults: List<String>?, custom: String?): List<Pair<String, String>> {
    val items = mutableListOf("" to fallbackLabel)
    defaults.orEmpty().filter { it.isNotBlank() }.forEachIndexed { index, value ->
        items += value to "默认$defaultPrefix${index + 1}"
    }
    custom?.takeIf { it.isNotBlank() }?.let { items += it to CustomAssetLabel }
    return items.distinctBy { it.first }
}

internal fun createImagePart(context: Context, uri: Uri): MultipartBody.Part? {
    val resolver = context.contentResolver
    val mimeType = resolver.getType(uri) ?: "image/*"
    val fileName = resolver.query(uri, null, null, null, null)?.use { cursor ->
        val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (index >= 0 && cursor.moveToFirst()) cursor.getString(index) else null
    } ?: "profile-image.jpg"
    val bytes = resolver.openInputStream(uri)?.use { it.readBytes() } ?: return null
    val body = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
    return MultipartBody.Part.createFormData("file", fileName, body)
}

internal fun resolveAssetUrl(value: String?): String? {
    val raw = value?.takeIf { it.isNotBlank() } ?: return null
    if (raw.startsWith("http://") || raw.startsWith("https://")) return raw
    val path = if (raw.startsWith('/')) raw else "/$raw"
    return ServerConfigHolder.current().staticBaseUrl().trimEnd('/') + path
}

internal fun isSameAsset(left: String, right: String): Boolean = comparableAsset(left) == comparableAsset(right)

private fun comparableAsset(value: String): String {
    if (value.isBlank()) return ""
    val resolved = resolveAssetUrl(value) ?: return ""
    return runCatching {
        val uri = URI(resolved)
        val path = uri.path.orEmpty()
        if (path.startsWith("/avatar/") || path.startsWith("/background/") || path.startsWith("/wallpaper/")) path else resolved
    }.getOrElse { value.trim() }
}
