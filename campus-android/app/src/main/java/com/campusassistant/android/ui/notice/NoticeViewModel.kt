package com.campusassistant.android.ui.notice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.campusassistant.android.core.datastore.NoticeStore
import com.campusassistant.android.core.network.ServerConfigHolder
import com.campusassistant.android.data.model.PublicNotice
import com.campusassistant.android.data.repository.AuthRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NoticeUiState(
    val latestNotice: PublicNotice? = null,
    val history: List<PublicNotice> = emptyList(),
    val activeNotice: PublicNotice? = null,
    val showNoticeDialog: Boolean = false,
    val showHistoryDialog: Boolean = false,
    val hasUnread: Boolean = false,
    val loading: Boolean = false
)

class NoticeViewModel(
    private val authRepository: AuthRepository,
    private val noticeStore: NoticeStore
) : ViewModel() {
    private val _uiState = MutableStateFlow(NoticeUiState())
    val uiState: StateFlow<NoticeUiState> = _uiState.asStateFlow()
    private var configWatchJob: Job? = null

    init {
        viewModelScope.launch {
            val latest = noticeStore.getLatestNotice()
            val history = noticeStore.getHistory()
            val shownVersion = noticeStore.getShownVersion()
            _uiState.update {
                it.copy(
                    latestNotice = latest,
                    history = history,
                    hasUnread = latest?.enabled == true && !latest.content.isNullOrBlank() && (latest.version ?: 0) > shownVersion
                )
            }
        }
    }

    fun start() {
        if (configWatchJob != null) return
        configWatchJob = viewModelScope.launch {
            ServerConfigHolder.config.collectLatest {
                delay(250)
                refreshNotice()
            }
        }
    }

    fun refreshNotice() {
        if (_uiState.value.loading) return
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true) }
            authRepository.getPublicNotice()
                .onSuccess { apiResult ->
                    val notice = apiResult.data
                    if (notice != null) {
                        noticeStore.saveLatestNotice(notice)
                        val shownVersion = noticeStore.getShownVersion()
                        val history = noticeStore.getHistory()
                        _uiState.update {
                            it.copy(
                                latestNotice = notice,
                                history = history,
                                hasUnread = notice.enabled == true && !notice.content.isNullOrBlank() && (notice.version ?: 0) > shownVersion,
                                loading = false
                            )
                        }
                    } else {
                        _uiState.update { it.copy(loading = false) }
                    }
                }
                .onFailure {
                    _uiState.update { current -> current.copy(loading = false) }
                }
        }
    }

    fun openLatestNotice() {
        val notice = _uiState.value.latestNotice ?: return
        viewModelScope.launch {
            markShownIfNeeded(notice)
            _uiState.update {
                it.copy(
                    activeNotice = notice,
                    showNoticeDialog = true,
                    showHistoryDialog = false,
                    hasUnread = false
                )
            }
        }
    }

    fun openHistoryDialog() {
        _uiState.update { it.copy(showHistoryDialog = true, showNoticeDialog = false) }
    }

    fun openHistoryNotice(notice: PublicNotice) {
        viewModelScope.launch {
            markShownIfNeeded(notice)
            _uiState.update {
                it.copy(
                    activeNotice = notice,
                    showNoticeDialog = true,
                    showHistoryDialog = false,
                    hasUnread = if (it.latestNotice?.version == notice.version) false else it.hasUnread
                )
            }
        }
    }

    fun closeDialogs() {
        _uiState.update { it.copy(showNoticeDialog = false, showHistoryDialog = false) }
    }

    private suspend fun markShownIfNeeded(notice: PublicNotice) {
        val version = notice.version ?: return
        val shownVersion = noticeStore.getShownVersion()
        if (version > shownVersion) {
            noticeStore.markShown(version)
        }
    }
}

class NoticeViewModelFactory(
    private val authRepository: AuthRepository,
    private val noticeStore: NoticeStore
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NoticeViewModel::class.java)) {
            return NoticeViewModel(authRepository, noticeStore) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
