package com.campusassistant.android.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.campusassistant.android.core.datastore.ServerConfigStore
import com.campusassistant.android.core.network.ServerConfig
import com.campusassistant.android.core.network.ServerConfigHolder
import com.campusassistant.android.data.model.ApiResult
import com.campusassistant.android.data.model.CustomAssets
import com.campusassistant.android.data.model.DefaultAssetOptions
import com.campusassistant.android.data.model.PersonalizationProfile
import com.campusassistant.android.data.model.PersonalizationUpdateRequest
import com.campusassistant.android.data.model.UserPersonal
import com.campusassistant.android.data.model.UserStatus
import com.campusassistant.android.data.repository.PersonalizationRepository
import com.campusassistant.android.data.repository.UserRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ServerSettingsDraft(
    val host: String = ServerConfig().host,
    val port: String = ServerConfig().port,
    val expanded: Boolean = false
)

data class PersonalizationSectionState(
    val expanded: Boolean = true
)

data class PersonalizationDraft(
    val avatar: String = "",
    val background: String = "",
    val wallpaper: String = "",
    val cardOpacity: Float = 1f,
    val cardBlur: Float = 14f,
    val globalFontEnabled: Boolean = true,
    val wallpaperMask: Float = 1f
) {
    fun toRequest(): PersonalizationUpdateRequest = PersonalizationUpdateRequest(
        avatar = avatar,
        background = background,
        wallpaper = wallpaper,
        cardOpacity = cardOpacity.toDouble(),
        cardBlur = cardBlur.toDouble(),
        globalFontEnabled = if (globalFontEnabled) 1 else 0,
        wallpaperMask = wallpaperMask.toDouble()
    )
}

data class ProfileUiState(
    val loading: Boolean = false,
    val updatingAutoPunch: Boolean = false,
    val savingPersonalization: Boolean = false,
    val savingServerConfig: Boolean = false,
    val deletingAccount: Boolean = false,
    val uploadingAssetType: String? = null,
    val personalResult: ApiResult<UserPersonal>? = null,
    val statusResult: ApiResult<UserStatus>? = null,
    val personalizationResult: ApiResult<PersonalizationProfile>? = null,
    val defaultOptionsResult: ApiResult<DefaultAssetOptions>? = null,
    val customAssetsResult: ApiResult<CustomAssets>? = null,
    val personalizationDraft: PersonalizationDraft = PersonalizationDraft(),
    val personalizationSectionState: PersonalizationSectionState = PersonalizationSectionState(),
    val serverSettingsDraft: ServerSettingsDraft = ServerSettingsDraft(),
    val errorMessage: String? = null,
    val actionMessage: String? = null
) {
    val personal: UserPersonal?
        get() = personalResult?.data

    val status: UserStatus?
        get() = statusResult?.data

    val personalization: PersonalizationProfile?
        get() = personalizationResult?.data

    val defaultOptions: DefaultAssetOptions?
        get() = defaultOptionsResult?.data.withFallbackDefaults()

    val customAssets: CustomAssets?
        get() = customAssetsResult?.data

    val autoPunchEnabled: Boolean
        get() = status?.autoPunchEnabled == 1

    val globalFontEnabled: Boolean
        get() = personalizationDraft.globalFontEnabled
}

class ProfileViewModel(
    private val userRepository: UserRepository,
    private val personalizationRepository: PersonalizationRepository,
    private val serverConfigStore: ServerConfigStore
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun reset(preserveServerSettings: Boolean = true) {
        val current = _uiState.value
        _uiState.value = if (preserveServerSettings) {
            ProfileUiState(
                serverSettingsDraft = current.serverSettingsDraft,
                personalizationSectionState = current.personalizationSectionState
            )
        } else {
            ProfileUiState()
        }
    }

    fun loadServerSettings() {
        viewModelScope.launch {
            val config = serverConfigStore.getConfig()
            ServerConfigHolder.update(config)
            _uiState.update { current ->
                current.copy(
                    serverSettingsDraft = current.serverSettingsDraft.copy(
                        host = config.host,
                        port = config.port
                    )
                )
            }
        }
    }

    fun loadProfile() {
        if (_uiState.value.loading) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(loading = true, errorMessage = null, actionMessage = null)
            }

            val personalDeferred = async { userRepository.getPersonal() }
            val statusDeferred = async { userRepository.getStatus() }
            val personalizationDeferred = async { personalizationRepository.getProfile() }
            val defaultOptionsDeferred = async { personalizationRepository.getDefaultOptions() }
            val customAssetsDeferred = async { personalizationRepository.getCustomAssets() }

            val personalResult = personalDeferred.await()
            val statusResult = statusDeferred.await()
            val personalizationResult = personalizationDeferred.await()
            val defaultOptionsResult = defaultOptionsDeferred.await()
            val customAssetsResult = customAssetsDeferred.await()

            _uiState.update { current ->
                val personalApiResult = personalResult.getOrNull()
                val statusApiResult = statusResult.getOrNull()
                val personalizationApiResult = personalizationResult.getOrNull()
                val defaultOptionsApiResult = defaultOptionsResult.getOrNull()
                val customAssetsApiResult = customAssetsResult.getOrNull()
                val personalization = personalizationApiResult?.takeIf { it.isSuccess }?.data
                val defaultOptions = defaultOptionsApiResult?.takeIf { it.isSuccess }?.data.withFallbackDefaults()

                val errorMessages = listOfNotNull(
                    personalResult.exceptionOrNull()?.message,
                    statusResult.exceptionOrNull()?.message,
                    personalizationResult.exceptionOrNull()?.message,
                    defaultOptionsResult.exceptionOrNull()?.message,
                    customAssetsResult.exceptionOrNull()?.message,
                    personalApiResult?.takeUnless { it.isSuccess }?.message,
                    statusApiResult?.takeUnless { it.isSuccess }?.message,
                    personalizationApiResult?.takeUnless { it.isSuccess }?.message,
                    defaultOptionsApiResult?.takeUnless { it.isSuccess }?.message,
                    customAssetsApiResult?.takeUnless { it.isSuccess }?.message
                ).map { it.trim() }.filter { it.isNotBlank() }.distinct()

                current.copy(
                    loading = false,
                    personalResult = personalApiResult ?: current.personalResult,
                    statusResult = statusApiResult ?: current.statusResult,
                    personalizationResult = personalizationApiResult ?: current.personalizationResult,
                    defaultOptionsResult = defaultOptionsApiResult?.copy(data = defaultOptions) ?: current.defaultOptionsResult,
                    customAssetsResult = customAssetsApiResult ?: current.customAssetsResult,
                    personalizationDraft = personalization?.toDraft() ?: current.personalizationDraft,
                    errorMessage = errorMessages.joinToString("；").ifBlank { null }
                )
            }
        }
    }

    fun updateAutoPunch(enabled: Boolean) {
        if (_uiState.value.updatingAutoPunch) return
        val oldStatusResult = _uiState.value.statusResult ?: return
        val oldStatus = oldStatusResult.data ?: return
        val optimisticStatus = oldStatus.copy(autoPunchEnabled = if (enabled) 1 else 0)

        viewModelScope.launch {
            _uiState.update { current ->
                current.copy(
                    updatingAutoPunch = true,
                    statusResult = oldStatusResult.copy(data = optimisticStatus),
                    errorMessage = null,
                    actionMessage = null
                )
            }

            userRepository.updateAutoPunch(enabled)
                .onSuccess { result ->
                    if (result.isSuccess) {
                        _uiState.update { current ->
                            current.copy(
                                updatingAutoPunch = false,
                                actionMessage = "自动打卡已更新"
                            )
                        }
                        loadProfile()
                    } else {
                        rollbackAutoPunch(oldStatusResult, result.message ?: "自动打卡更新失败")
                    }
                }
                .onFailure { throwable ->
                    rollbackAutoPunch(oldStatusResult, throwable.message ?: "自动打卡更新失败")
                }
        }
    }

    fun selectAvatar(value: String) = updateDraft { it.copy(avatar = value) }

    fun selectBackground(value: String) = updateDraft { it.copy(background = value) }

    fun selectWallpaper(value: String) = updateDraft { it.copy(wallpaper = value) }

    fun updateCardOpacity(value: Float) = updateDraft { it.copy(cardOpacity = value.coerceIn(0.2f, 1f)) }

    fun updateCardBlur(value: Float) = updateDraft { it.copy(cardBlur = value.coerceIn(0f, 30f)) }

    fun updateWallpaperMask(value: Float) = updateDraft { it.copy(wallpaperMask = value.coerceIn(0f, 1f)) }

    fun updateGlobalFont(enabled: Boolean) = updateDraft { it.copy(globalFontEnabled = enabled) }

    fun togglePersonalizationExpanded() {
        _uiState.update { current ->
            current.copy(
                personalizationSectionState = current.personalizationSectionState.copy(
                    expanded = !current.personalizationSectionState.expanded
                )
            )
        }
    }

    fun toggleServerSettingsExpanded() {
        _uiState.update { current ->
            current.copy(
                serverSettingsDraft = current.serverSettingsDraft.copy(
                    expanded = !current.serverSettingsDraft.expanded
                )
            )
        }
    }

    fun updateServerHost(value: String) {
        _uiState.update { current ->
            current.copy(
                serverSettingsDraft = current.serverSettingsDraft.copy(host = value.trim()),
                errorMessage = null,
                actionMessage = null
            )
        }
    }

    fun updateServerPort(value: String) {
        _uiState.update { current ->
            current.copy(
                serverSettingsDraft = current.serverSettingsDraft.copy(port = value.filter { it.isDigit() }.take(5)),
                errorMessage = null,
                actionMessage = null
            )
        }
    }

    fun saveServerSettings() {
        if (_uiState.value.savingServerConfig) return
        val draft = _uiState.value.serverSettingsDraft
        val config = ServerConfig(host = draft.host, port = draft.port)
        if (!config.isValid()) {
            _uiState.update { it.copy(errorMessage = "服务器地址格式无效，请检查 IP/域名 和端口") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(savingServerConfig = true, errorMessage = null, actionMessage = null) }
            serverConfigStore.saveConfig(config)
            ServerConfigHolder.update(config)
            _uiState.update {
                it.copy(
                    savingServerConfig = false,
                    actionMessage = "服务器设置已保存，后续请求将使用 ${config.host}:${config.port}"
                )
            }
        }
    }

    fun savePersonalization() {
        if (_uiState.value.savingPersonalization) return
        val request = _uiState.value.personalizationDraft.toRequest()
        viewModelScope.launch {
            _uiState.update { it.copy(savingPersonalization = true, errorMessage = null, actionMessage = null) }
            personalizationRepository.updateProfile(request)
                .onSuccess { result ->
                    if (result.isSuccess) {
                        _uiState.update { current ->
                            current.copy(
                                savingPersonalization = false,
                                personalizationResult = result,
                                personalizationDraft = result.data?.toDraft() ?: current.personalizationDraft,
                                actionMessage = "个性化配置已保存"
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                savingPersonalization = false,
                                errorMessage = result.message ?: "保存个性化配置失败"
                            )
                        }
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            savingPersonalization = false,
                            errorMessage = throwable.message ?: "保存个性化配置失败"
                        )
                    }
                }
        }
    }

    fun uploadAsset(type: String, file: okhttp3.MultipartBody.Part) {
        if (_uiState.value.uploadingAssetType != null) return
        viewModelScope.launch {
            _uiState.update { it.copy(uploadingAssetType = type, errorMessage = null, actionMessage = null) }
            personalizationRepository.uploadCustomAsset(type, file)
                .onSuccess { result ->
                    val url = result.data?.url.orEmpty()
                    if (!result.isSuccess || url.isBlank()) {
                        _uiState.update {
                            it.copy(
                                uploadingAssetType = null,
                                errorMessage = result.message ?: "上传图片失败"
                            )
                        }
                        return@onSuccess
                    }
                    val nextDraft = _uiState.value.personalizationDraft.withAsset(type, url)
                    _uiState.update { it.copy(personalizationDraft = nextDraft) }
                    saveUploadedSelection(nextDraft, type)
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            uploadingAssetType = null,
                            errorMessage = throwable.message ?: "上传图片失败"
                        )
                    }
                }
        }
    }

    private suspend fun saveUploadedSelection(draft: PersonalizationDraft, type: String) {
        personalizationRepository.updateProfile(draft.toRequest())
            .onSuccess { result ->
                if (result.isSuccess) {
                    _uiState.update { current ->
                        current.copy(
                            uploadingAssetType = null,
                            personalizationResult = result,
                            personalizationDraft = result.data?.toDraft() ?: draft,
                            actionMessage = "${assetTypeLabel(type)}已上传并保存"
                        )
                    }
                    loadProfile()
                } else {
                    _uiState.update {
                        it.copy(
                            uploadingAssetType = null,
                            errorMessage = result.message ?: "上传成功，但保存选择失败"
                        )
                    }
                }
            }
            .onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        uploadingAssetType = null,
                        errorMessage = throwable.message ?: "上传成功，但保存选择失败"
                    )
                }
            }
    }

    private fun updateDraft(block: (PersonalizationDraft) -> PersonalizationDraft) {
        _uiState.update { current ->
            current.copy(
                personalizationDraft = block(current.personalizationDraft),
                errorMessage = null,
                actionMessage = null
            )
        }
    }

    fun deleteAccount() {
        if (_uiState.value.deletingAccount) return
        viewModelScope.launch {
            _uiState.update { it.copy(deletingAccount = true, errorMessage = null, actionMessage = null) }
            userRepository.deleteAccount()
                .onSuccess { result ->
                    if (result.isSuccess) {
                        _uiState.value = ProfileUiState(actionMessage = result.data ?: result.message ?: "账号已注销")
                    } else {
                        _uiState.update {
                            it.copy(
                                deletingAccount = false,
                                errorMessage = result.message ?: "注销账号失败"
                            )
                        }
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            deletingAccount = false,
                            errorMessage = throwable.message ?: "注销账号失败"
                        )
                    }
                }
        }
    }

    private fun rollbackAutoPunch(
        oldStatusResult: ApiResult<UserStatus>,
        message: String
    ) {
        _uiState.update { current ->
            current.copy(
                updatingAutoPunch = false,
                statusResult = oldStatusResult,
                errorMessage = message,
                actionMessage = null
            )
        }
    }
}

class ProfileViewModelFactory(
    private val userRepository: UserRepository,
    private val personalizationRepository: PersonalizationRepository,
    private val serverConfigStore: ServerConfigStore
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel(userRepository, personalizationRepository, serverConfigStore) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

private fun PersonalizationProfile.toDraft(): PersonalizationDraft = PersonalizationDraft(
    avatar = avatar.orEmpty(),
    background = background.orEmpty(),
    wallpaper = wallpaper.orEmpty(),
    cardOpacity = (cardOpacity ?: 1.0).toFloat().coerceIn(0.2f, 1f),
    cardBlur = (cardBlur ?: 14.0).toFloat().coerceIn(0f, 30f),
    globalFontEnabled = globalFontEnabled != 0,
    wallpaperMask = (wallpaperMask ?: 1.0).toFloat().coerceIn(0f, 1f)
)

private fun PersonalizationDraft.withAsset(type: String, url: String): PersonalizationDraft = when (type) {
    "avatar" -> copy(avatar = url)
    "background" -> copy(background = url)
    "wallpaper" -> copy(wallpaper = url)
    else -> this
}

private fun assetTypeLabel(type: String): String = when (type) {
    "avatar" -> "头像"
    "background" -> "顶部背景"
    "wallpaper" -> "墙纸"
    else -> "图片"
}

private val DefaultFallbackOptions = DefaultAssetOptions(
    avatars = listOf("/avatar/avatar_default_1.jpg", "/avatar/avatar_default_2.jpg"),
    backgrounds = listOf("/background/background_default_1.jpg", "/background/background_default_2.jpg"),
    wallpapers = listOf("/wallpaper/wallpaper_default_1.jpg", "/wallpaper/wallpaper_default_2.jpg")
)

private fun DefaultAssetOptions?.withFallbackDefaults(): DefaultAssetOptions = DefaultAssetOptions(
    avatars = this?.avatars.takeUnless { it.isNullOrEmpty() } ?: DefaultFallbackOptions.avatars,
    backgrounds = this?.backgrounds.takeUnless { it.isNullOrEmpty() } ?: DefaultFallbackOptions.backgrounds,
    wallpapers = this?.wallpapers.takeUnless { it.isNullOrEmpty() } ?: DefaultFallbackOptions.wallpapers
)
