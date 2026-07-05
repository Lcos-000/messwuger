<template>
  <div class="section-card info-card">
    <div
      v-for="field in profileViewConfig.INFO_FIELDS"
      :key="field.key"
      class="info-row"
      v-show="userInfoLoaded"
    >
      <div class="info-icon" :style="field.iconStyle">
        <svg
          v-if="field.icon === iconMap.college"
          width="16"
          height="16"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          stroke-width="2"
          stroke-linecap="round"
          stroke-linejoin="round"
        >
          <path d="M3 21v-8M21 21v-8M3 13L12 3l9 10M12 3v18" />
        </svg>
        <svg
          v-else-if="field.icon === iconMap.major"
          width="16"
          height="16"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          stroke-width="2"
          stroke-linecap="round"
          stroke-linejoin="round"
        >
          <path d="M22 10v6M2 10l10-5 10 5-10 5z" />
          <path d="M6 12v5c3 3 9 3 12 0v-5" />
        </svg>
        <svg
          v-else
          width="16"
          height="16"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          stroke-width="2"
          stroke-linecap="round"
          stroke-linejoin="round"
        >
          <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z" />
          <polyline points="9 22 9 12 15 12 15 22" />
        </svg>
      </div>

      <div class="info-content">
        <span class="info-label">{{ field.label }}</span>
        <span class="info-value">{{ personalInfo[field.key] || appConfig[field.fallback] }}</span>
      </div>
    </div>

    <div class="info-row" v-show="userInfoLoaded">
      <div class="info-icon" :style="syncIconStyle">
        <svg
          width="16"
          height="16"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          stroke-width="2"
          stroke-linecap="round"
          stroke-linejoin="round"
        >
          <polyline points="23 4 23 10 17 10" />
          <polyline points="1 20 1 14 7 14" />
          <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15" />
        </svg>
      </div>
      <div class="info-content">
        <span class="info-label">同步状态</span>
        <span class="info-value" :class="statusClass">{{ statusText }}</span>
      </div>
    </div>

    <div class="info-row" v-show="userInfoLoaded">
      <div class="info-icon" :style="punchIconStyle">
        <svg
          width="16"
          height="16"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          stroke-width="2"
          stroke-linecap="round"
          stroke-linejoin="round"
        >
          <circle cx="12" cy="12" r="10" />
          <polyline points="12 6 12 12 16 14" />
        </svg>
      </div>
      <div class="info-content">
        <span class="info-label">{{ profileViewConfig.STATUS_FIELDS[1].label }}</span>
        <span class="info-value" :class="punchClass">{{ punchText }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  profileViewConfig: {
    type: Object,
    required: true
  },
  appConfig: {
    type: Object,
    required: true
  },
  iconMap: {
    type: Object,
    required: true
  },
  personalInfo: {
    type: Object,
    required: true
  },
  userInfoLoaded: {
    type: Boolean,
    required: true
  },
  syncIconStyle: {
    type: [Object, String],
    required: true
  },
  statusClass: {
    type: String,
    required: true
  },
  statusText: {
    type: String,
    required: true
  },
  punchIconStyle: {
    type: [Object, String],
    required: true
  },
  punchClass: {
    type: String,
    required: true
  },
  punchText: {
    type: String,
    required: true
  }
})
</script>

<style scoped>
.info-card {
  overflow: visible;
  padding-top: 10px;
}

.info-row {
  display: flex;
  align-items: center;
  padding: 14px 18px;
  gap: 14px;
  border-bottom: 1px solid #f0f4fb;
}

.info-row:last-child {
  border-bottom: none;
}

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
  letter-spacing: 0.3px;
}

.info-value {
  font-size: 14px;
  font-weight: 600;
  color: #1a2540;
}

.status-ok {
  color: #10b981;
}

.status-syncing {
  color: #4f86f7;
}

.status-err {
  color: #ef4444;
}
</style>
