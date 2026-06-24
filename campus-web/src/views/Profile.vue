<template>
  <div class="profile-root">
    <!-- 顶部背景渐变 + 用户信息 -->
    <div class="hero-section">
      <div class="hero-bg"></div>
      <div class="avatar-ring">
        <div class="avatar">{{ firstChar }}</div>
      </div>
      <h2 class="user-name">{{ personalInfo.name || APP_CONFIG.DEFAULT_USER_NAME }}</h2>
      <p class="user-sub">{{ personalInfo.studentId || tokenInfo?.studentID || APP_CONFIG.UNKNOWN_STUDENT_ID }}</p>
    </div>

    <!-- 信息卡片 -->
    <div class="section-card info-card">
      <div class="info-row" v-show="userInfoLoaded">
        <div class="info-icon" style="background:#fef3c7; color:#f59e0b">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 21v-8M21 21v-8M3 13L12 3l9 10M12 3v18"/></svg>
        </div>
        <div class="info-content">
          <span class="info-label">学校/学院</span>
          <span class="info-value">{{ personalInfo.college || APP_CONFIG.DEFAULT_COLLEGE }}</span>
        </div>
      </div>
      <div class="info-row" v-show="userInfoLoaded">
        <div class="info-icon" style="background:#eef3ff; color:#4f86f7">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 10v6M2 10l10-5 10 5-10 5z"/><path d="M6 12v5c3 3 9 3 12 0v-5"/></svg>
        </div>
        <div class="info-content">
          <span class="info-label">专业</span>
          <span class="info-value">{{ personalInfo.major || APP_CONFIG.DEFAULT_USER_NAME }}</span>
        </div>
      </div>
      <div class="info-row" v-show="userInfoLoaded">
        <div class="info-icon" style="background:#f0fdf4; color:#10b981">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
        </div>
        <div class="info-content">
          <span class="info-label">班级</span>
          <span class="info-value">{{ personalInfo.className || APP_CONFIG.DEFAULT_USER_NAME }}</span>
        </div>
      </div>
      <div class="info-row" v-show="userInfoLoaded">
        <div class="info-icon" :style="syncIconStyle">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="23 4 23 10 17 10"/><polyline points="1 20 1 14 7 14"/><path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"/></svg>
        </div>
        <div class="info-content">
          <span class="info-label">同步状态</span>
          <span class="info-value" :class="statusClass">{{ statusText }}</span>
        </div>
      </div>
      <div class="info-row" v-show="userInfoLoaded">
        <div class="info-icon" :style="punchIconStyle">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
        </div>
        <div class="info-content">
          <span class="info-label">打卡状态</span>
          <span class="info-value" :class="punchClass">{{ punchText }}</span>
        </div>
      </div>
    </div>

    <!-- 操作区 -->
    <div class="section-card action-card">
      <!-- 测试阶段未开启开关功能，当前全部默认自动打卡 -->
      <button class="action-row" @click="handleAutoPunch">
        <div class="action-icon" style="background:#f0fdf4; color:#10b981">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
        </div>
        <span class="action-label">开启自动打卡</span>
        <svg class="chevron" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><polyline points="9 18 15 12 9 6"/></svg>
      </button>
    </div>

    <div class="section-card action-card danger-card">
      <button class="action-row" @click="handleLogout">
        <div class="action-icon" style="background:#fff7ed; color:#f97316">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg>
        </div>
        <span class="action-label" style="color:#f97316">退出登录</span>
        <svg class="chevron" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#f97316" stroke-width="2" stroke-linecap="round"><polyline points="9 18 15 12 9 6"/></svg>
      </button>
      <div class="divider"></div>
      <button class="action-row" :disabled="deletingAccount" @click="handleDeleteAccount">
        <div class="action-icon" style="background:#fee2e2; color:#ef4444">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/><path d="M10 11v6"/><path d="M14 11v6"/><path d="M9 6V4a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2"/></svg>
        </div>
        <span class="action-label" style="color:#ef4444">{{ deletingAccount ? '注销中...' : '注销账号' }}</span>
        <svg class="chevron" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#ef4444" stroke-width="2" stroke-linecap="round"><polyline points="9 18 15 12 9 6"/></svg>
      </button>
    </div>

    <p class="footer-hint">数据来源于教务系统，仅供参考</p>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { logout, deleteAccount, getPersonalInfo, getUserStatus } from '@/api/index'
import {
  APP_CONFIG,
  HTTP_STATUS,
  POLLING_CONFIG,
  PUNCH_STATUS,
  PUNCH_STATUS_TEXT,
  ROUTE_PATHS,
  STATUS_CLASS,
  STATUS_ICON_STYLE,
  STORAGE_KEYS,
  SYNC_STATUS,
  SYNC_STATUS_TEXT,
  isUserStatusProcessing
} from '@/config'

const router = useRouter()
const personalInfo = ref({})
const userStatus = ref({})
const tokenInfo = ref(null)
const userInfoLoaded = ref(false)
const deletingAccount = ref(false)
let statusTimer = null

const getUserInfoFromToken = () => {
  const token = localStorage.getItem(STORAGE_KEYS.TOKEN)
  if (!token) return null
  try {
    const payload = JSON.parse(atob(token.split('.')[1]))
    return payload.student_login
  } catch {
    return null
  }
}

const fetchPersonalInfo = async () => {
  try {
    const res = await getPersonalInfo()
    if (res.code === HTTP_STATUS.SUCCESS && res.data) {
      personalInfo.value = res.data
    }
  } catch (error) {
    console.error('获取个人信息失败', error)
  } finally {
    userInfoLoaded.value = true
  }
}

const fetchUserStatus = async () => {
  try {
    const res = await getUserStatus()
    if (res.code === HTTP_STATUS.SUCCESS && res.data) {
      userStatus.value = res.data
      
      // 如果处于进行中状态，开启轮询
      if (isUserStatusProcessing(userStatus.value)) {
        startStatusPolling()
      } else {
        stopStatusPolling()
      }
    }
  } catch (error) {
    console.error('获取用户状态失败', error)
    stopStatusPolling()
  }
}

const startStatusPolling = () => {
  if (statusTimer) return
  statusTimer = setInterval(() => {
    fetchUserStatus()
  }, POLLING_CONFIG.USER_STATUS_INTERVAL)
}

const stopStatusPolling = () => {
  if (statusTimer) {
    clearInterval(statusTimer)
    statusTimer = null
  }
}

onMounted(() => {
  tokenInfo.value = getUserInfoFromToken()
  const userId = tokenInfo.value?.userid
  if (userId) {
    fetchPersonalInfo()
    fetchUserStatus()
  } else {
    userInfoLoaded.value = true
  }
})

onUnmounted(() => {
  stopStatusPolling()
})

const firstChar = computed(() => personalInfo.value.name ? personalInfo.value.name.charAt(0) : APP_CONFIG.DEFAULT_AVATAR_TEXT)

const statusText = computed(() => {
  const s = userStatus.value.syncStatus
  return SYNC_STATUS_TEXT[s] || SYNC_STATUS_TEXT.DEFAULT
})

const statusClass = computed(() => {
  const s = userStatus.value.syncStatus
  if (s === SYNC_STATUS.SUCCESS) return STATUS_CLASS.SUCCESS
  if (s === SYNC_STATUS.SYNCING) return STATUS_CLASS.PROCESSING
  if (s === SYNC_STATUS.FAILED) return STATUS_CLASS.FAILED
  return STATUS_CLASS.DEFAULT
})

const syncIconStyle = computed(() => {
  const s = userStatus.value.syncStatus
  if (s === SYNC_STATUS.SUCCESS) return STATUS_ICON_STYLE.SUCCESS
  if (s === SYNC_STATUS.SYNCING) return STATUS_ICON_STYLE.PROCESSING
  if (s === SYNC_STATUS.FAILED) return STATUS_ICON_STYLE.FAILED
  return STATUS_ICON_STYLE.DEFAULT
})

const punchText = computed(() => {
  const p = userStatus.value.punchStatus
  return PUNCH_STATUS_TEXT[p] || PUNCH_STATUS_TEXT.DEFAULT
})

const punchClass = computed(() => {
  const p = userStatus.value.punchStatus
  if (p === PUNCH_STATUS.SUCCESS) return STATUS_CLASS.SUCCESS
  if (p === PUNCH_STATUS.PUNCHING) return STATUS_CLASS.PROCESSING
  if (p === PUNCH_STATUS.FAILED) return STATUS_CLASS.FAILED
  return STATUS_CLASS.DEFAULT
})

const punchIconStyle = computed(() => {
  const p = userStatus.value.punchStatus
  if (p === PUNCH_STATUS.SUCCESS) return STATUS_ICON_STYLE.SUCCESS
  if (p === PUNCH_STATUS.PUNCHING) return STATUS_ICON_STYLE.PROCESSING
  if (p === PUNCH_STATUS.FAILED) return STATUS_ICON_STYLE.FAILED
  return STATUS_ICON_STYLE.DEFAULT
})

// 测试阶段未开启开关功能，当前全部默认自动打卡
const handleAutoPunch = () => {
  alert(APP_CONFIG.AUTO_PUNCH_TIP)
}

const clearAuthAndGoLogin = () => {
  stopStatusPolling()
  localStorage.removeItem(STORAGE_KEYS.TOKEN)
  personalInfo.value = {}
  userStatus.value = {}
  tokenInfo.value = null
  userInfoLoaded.value = false
  window.location.replace(ROUTE_PATHS.LOGIN)
}

const handleLogout = async () => {
  if (!confirm(APP_CONFIG.LOGOUT_CONFIRM)) return
  try {
    await logout()
  } catch (_) {}
  clearAuthAndGoLogin()
}

const handleDeleteAccount = async () => {
  if (deletingAccount.value) return
  if (!confirm(APP_CONFIG.DELETE_ACCOUNT_CONFIRM)) return

  deletingAccount.value = true
  try {
    await deleteAccount()
    try {
      await logout()
    } catch (_) {}
    clearAuthAndGoLogin()
  } catch (error) {
    console.error(error)
  } finally {
    deletingAccount.value = false
  }
}
</script>

<style scoped>
.profile-root {
  background: #f0f4fb;
  min-height: 100%;
  padding-bottom: 40px;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', sans-serif;
}

/* ---- Hero ---- */
.hero-section {
  position: relative;
  padding: 40px 24px 64px;
  text-align: center;
  overflow: hidden;
}
.hero-bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(145deg, #4f86f7 0%, #6366f1 100%);
  z-index: 0;
}
.hero-bg::after {
  content: '';
  position: absolute;
  bottom: -30px;
  left: -10%;
  right: -10%;
  height: 60px;
  background: #f0f4fb;
  border-radius: 50%;
}
.avatar-ring {
  position: relative;
  z-index: 1;
  display: inline-block;
  padding: 3px;
  background: rgba(255,255,255,.25);
  border-radius: 50%;
  margin-bottom: 12px;
}
.avatar {
  width: 72px;
  height: 72px;
  background: #fff;
  color: #4f86f7;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  font-weight: 800;
}
.user-name {
  position: relative;
  z-index: 1;
  font-size: 20px;
  font-weight: 700;
  color: #fff;
  margin-bottom: 4px;
}
.user-sub {
  position: relative;
  z-index: 1;
  font-size: 13px;
  color: rgba(255,255,255,.75);
}

/* ---- Cards ---- */
.section-card {
  margin: -8px 16px 14px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(15,25,50,.07);
  overflow: hidden;
}
.info-card {
  overflow: visible;
  padding-top: 10px;
}
.section-card + .section-card { margin-top: 14px; }

/* ---- Info rows ---- */
.info-row {
  display: flex;
  align-items: center;
  padding: 14px 18px;
  gap: 14px;
  border-bottom: 1px solid #f0f4fb;
}
.info-row:last-child { border-bottom: none; }
.info-icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.info-content {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.info-label {
  font-size: 11px;
  color: #b0bdd4;
  letter-spacing: .3px;
}
.info-value {
  font-size: 14px;
  font-weight: 600;
  color: #1a2540;
}
.status-ok { color: #10b981; }
.status-syncing { color: #4f86f7; }
.status-err { color: #ef4444; }

/* ---- Action rows ---- */
.action-card { margin-top: 0; }
.action-row {
  width: 100%;
  display: flex;
  align-items: center;
  padding: 14px 18px;
  gap: 14px;
  text-align: left;
  transition: background .15s;
}
.action-row:active { background: #f8faff; }
.action-row:disabled {
  opacity: 0.68;
  cursor: not-allowed;
}
.action-icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.action-label {
  flex: 1;
  font-size: 14px;
  font-weight: 500;
  color: #1a2540;
}
.chevron { flex-shrink: 0; color: #b0bdd4; }
.divider {
  height: 1px;
  background: #f0f4fb;
  margin: 0 18px;
}

/* ---- Footer ---- */
.footer-hint {
  text-align: center;
  font-size: 11px;
  color: #b0bdd4;
  margin-top: 24px;
  letter-spacing: .3px;
}
</style>
