<template>
  <div>
    <div class="section-card action-card">
      <div
        v-for="toggle in actionToggles"
        :key="toggle.key"
        class="action-row auto-punch-row"
      >
        <div class="action-icon" :style="toggle.iconStyle">
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

        <div class="action-main">
          <span class="action-label">{{ toggle.label }}</span>
          <span class="action-subtitle">{{ toggle.description }}</span>
        </div>

        <label class="toggle-switch" @click.stop>
          <input
            type="checkbox"
            :checked="toggle.currentValue"
            @change="emit('update-action-toggle', toggle.key, $event.target.checked)"
          />
          <span class="toggle-slider"></span>
        </label>
      </div>
    </div>

    <div class="section-card action-card danger-card">
      <button class="action-row" @click="emit('logout')">
        <div class="action-icon" :style="profileViewConfig.ACTIONS.logout.iconStyle">
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
            <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
            <polyline points="16 17 21 12 16 7" />
            <line x1="21" y1="12" x2="9" y2="12" />
          </svg>
        </div>
        <span class="action-label" :style="{ color: profileViewConfig.ACTIONS.logout.color }">
          {{ profileViewConfig.ACTIONS.logout.label }}
        </span>
        <svg
          class="chevron"
          width="16"
          height="16"
          viewBox="0 0 24 24"
          fill="none"
          :stroke="profileViewConfig.ACTIONS.logout.color"
          stroke-width="2"
          stroke-linecap="round"
        >
          <polyline points="9 18 15 12 9 6" />
        </svg>
      </button>

      <div class="divider"></div>

      <button class="action-row" :disabled="deletingAccount" @click="emit('delete-account')">
        <div class="action-icon" style="background:#fee2e2; color:#ef4444">
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
            <polyline points="3 6 5 6 21 6" />
            <path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6" />
            <path d="M10 11v6" />
            <path d="M14 11v6" />
            <path d="M9 6V4a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2" />
          </svg>
        </div>
        <span class="action-label" style="color:#ef4444">
          {{ deletingAccount ? '注销中...' : '注销账号' }}
        </span>
        <svg
          class="chevron"
          width="16"
          height="16"
          viewBox="0 0 24 24"
          fill="none"
          stroke="#ef4444"
          stroke-width="2"
          stroke-linecap="round"
        >
          <polyline points="9 18 15 12 9 6" />
        </svg>
      </button>
    </div>

    <p class="footer-hint">{{ profileViewConfig.FOOTER_HINT }}</p>
  </div>
</template>

<script setup>
defineProps({
  profileViewConfig: {
    type: Object,
    required: true
  },
  actionToggles: {
    type: Array,
    required: true
  },
  deletingAccount: {
    type: Boolean,
    required: true
  }
})

const emit = defineEmits(['update-action-toggle', 'logout', 'delete-account'])
</script>

<style scoped>
.section-card {
  margin: 0 0 14px;
  background: rgba(255, 255, 255, var(--card-opacity, 0.88));
  border-radius: 18px;
  overflow: hidden;
  box-shadow: 0 14px 36px rgba(15, 25, 50, 0.08);
  backdrop-filter: blur(var(--card-blur, 14px)) saturate(1.06);
  -webkit-backdrop-filter: blur(var(--card-blur, 14px)) saturate(1.06);
  border: 1px solid rgba(255, 255, 255, 0.5);
}

.section-card + .section-card {
  margin-top: 14px;
}
.toggle-switch {
  position: relative;
  display: inline-block;
  width: 44px;
  height: 24px;
  flex-shrink: 0;
}

.toggle-switch input {
  opacity: 0;
  width: 0;
  height: 0;
}

.toggle-slider {
  position: absolute;
  cursor: pointer;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: #cbd5e1;
  transition: 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border-radius: 24px;
}

.toggle-slider::before {
  position: absolute;
  content: '';
  height: 18px;
  width: 18px;
  left: 3px;
  bottom: 3px;
  background-color: white;
  transition: 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border-radius: 50%;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.15);
}

input:checked + .toggle-slider {
  background-color: var(--hero-theme-start);
}

input:checked + .toggle-slider::before {
  transform: translateX(20px);
}

.action-card {
  margin-top: 0;
}

.action-row {
  width: 100%;
  display: flex;
  align-items: center;
  padding: 14px 18px;
  gap: 14px;
  text-align: left;
  transition: background 0.15s;
}

.auto-punch-row {
  justify-content: space-between;
}

.action-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.action-subtitle {
  font-size: 12px;
  color: #94a3b8;
  line-height: 1.4;
}

.action-row:active {
  background: #f8faff;
}

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

.chevron {
  flex-shrink: 0;
  color: #b0bdd4;
}

.divider {
  height: 1px;
  background: #f0f4fb;
  margin: 0 18px;
}

.footer-hint {
  text-align: center;
  font-size: 11px;
  color: #b0bdd4;
  margin-top: 24px;
  letter-spacing: 0.3px;
}
</style>



