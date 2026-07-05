<template>
  <div
    class="section-card gallery-card"
    :class="{ expanded: galleryExpanded }"
    @click="toggleGalleryExpanded"
  >
    <div class="gallery-toggle-main">
      <div class="gallery-title-row">
        <span class="gallery-toggle-title">{{ profileViewConfig.GALLERY_TITLE }}</span>
        <button
          type="button"
          class="help-icon-wrap"
          :class="{ active: activeTooltipKey === 'gallery-help' }"
          aria-label="查看提示"
          @click.stop="toggleTooltip('gallery-help')"
        >
          <span class="help-icon" aria-hidden="true">
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.1" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="12" cy="12" r="9"></circle>
              <path d="M9.6 9.2a2.5 2.5 0 0 1 4.8.9c0 1.6-1.4 2.3-2.2 2.9-.6.4-.9.8-.9 1.5"></path>
              <circle cx="12" cy="17.2" r="0.8" fill="currentColor" stroke="none"></circle>
            </svg>
          </span>
          <span class="help-tooltip">{{ profileViewConfig.GALLERY_HELP_TEXT }}</span>
        </button>
      </div>
    </div>
    <span class="gallery-toggle-arrow" :class="{ expanded: galleryExpanded }">⌄</span>

    <transition name="gallery-collapse">
      <div v-if="galleryExpanded" class="gallery-panel" @click.stop>
        <div class="gallery-settings">
          <div class="gallery-settings-actions">
            <button type="button" class="reset-profile-button" @click="emit('reset-profile')">
              {{ profileViewConfig.RESET_PROFILE_TEXT }}
            </button>
          </div>
          <div
            v-for="setting in displaySettings"
            :key="setting.key"
            class="setting-block"
            :class="{ 'font-setting-block': setting.type === 'toggle' }"
          >
            <div class="setting-head">
              <div class="setting-title-row">
                <span class="setting-title">{{ setting.title }}</span>
                <button
                  type="button"
                  class="help-icon-wrap"
                  :class="{ active: activeTooltipKey === `setting-${setting.key}` }"
                  aria-label="查看提示"
                  @click.stop="toggleTooltip(`setting-${setting.key}`)"
                >
                  <span class="help-icon" aria-hidden="true">
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.1" stroke-linecap="round" stroke-linejoin="round">
                      <circle cx="12" cy="12" r="9"></circle>
                      <path d="M9.6 9.2a2.5 2.5 0 0 1 4.8.9c0 1.6-1.4 2.3-2.2 2.9-.6.4-.9.8-.9 1.5"></path>
                      <circle cx="12" cy="17.2" r="0.8" fill="currentColor" stroke="none"></circle>
                    </svg>
                  </span>
                  <span class="help-tooltip">{{ setting.helpText }}</span>
                </button>
              </div>

              <span v-if="setting.type === 'range'" class="setting-value">
                {{ formatDisplaySettingValue(setting) }}
              </span>
              <label v-else class="toggle-switch">
                <input
                  type="checkbox"
                  :checked="setting.currentValue"
                  @change="emit('update-display-setting', setting.key, $event.target.checked)"
                />
                <span class="toggle-slider"></span>
              </label>
            </div>

            <input
              v-if="setting.type === 'range'"
              :value="setting.currentValue"
              class="setting-slider"
              type="range"
              :min="setting.min"
              :max="setting.max"
              :step="setting.step"
              @input="emit('update-display-setting', setting.key, $event.target.value)"
            />
          </div>
        </div>

        <div
          v-for="group in galleryGroups"
          :key="group.key"
          class="gallery-block"
        >
          <div class="gallery-head">
            <span class="gallery-title">{{ group.title }}</span>
            <span class="gallery-tip">{{ profileViewConfig.OPTION_GROUP_HINT }}</span>
          </div>

          <div class="gallery-grid" :class="group.gridClass">
            <div
              v-for="option in group.options"
              :key="option.key"
              class="gallery-option-shell"
              :class="{ 'gallery-option-shell--with-caption': !!option.caption }"
            >
              <button
                type="button"
                class="gallery-option"
                :style="option.style || null"
                :data-upload-label="option.type === 'upload' && option.previewUrl && group.key !== 'avatar' ? profileViewConfig.REPLACE_TILE_TEXT : ''"
                :class="[
                  group.optionClass,
                  `gallery-option--${option.type}`,
                  { active: isGalleryOptionActive(group.key, option), 'gallery-option--custom': option.source === 'custom' }
                ]"
                @click="emit('gallery-option-click', group.key, option)"
              >
                <template v-if="option.type === 'upload'">
                  <span class="upload-option-body">
                    <span class="upload-option-icon">+</span>
                    <span v-if="!option.previewUrl" class="upload-option-title">{{ profileViewConfig.UPLOAD_TILE_TEXT }}</span>
                    <span v-if="!option.previewUrl" class="upload-option-subtitle">{{ profileViewConfig.UPLOAD_TILE_SUBTEXT }}</span>
                  </span>
                </template>
                <template v-else>
                  <img :src="option.url" :alt="group.imageAlt" />
                  <span v-if="option.source === 'custom'" class="custom-badge">{{ profileViewConfig.CUSTOM_BADGE_TEXT }}</span>
                </template>
              </button>
              <span v-if="option.caption" class="gallery-option-caption">{{ option.caption }}</span>
            </div>
            <input
              :ref="(element) => emit('file-input-ref', group.key, element)"
              class="gallery-file-input"
              type="file"
              accept="image/*"
              @change="emit('local-file-change', group.key, $event)"
            />
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, ref } from 'vue'

const props = defineProps({
  profileViewConfig: {
    type: Object,
    required: true
  },
  displaySettings: {
    type: Array,
    required: true
  },
  galleryGroups: {
    type: Array,
    required: true
  },
  formatDisplaySettingValue: {
    type: Function,
    required: true
  },
  isGalleryOptionActive: {
    type: Function,
    required: true
  }
})

const emit = defineEmits([
  'reset-profile',
  'update-display-setting',
  'gallery-option-click',
  'file-input-ref',
  'local-file-change'
])

const galleryExpanded = ref(false)
const activeTooltipKey = ref('')

const toggleGalleryExpanded = () => {
  closeTooltip()
  galleryExpanded.value = !galleryExpanded.value
}

const toggleTooltip = (key) => {
  activeTooltipKey.value = activeTooltipKey.value === key ? '' : key
}

const closeTooltip = () => {
  activeTooltipKey.value = ''
}

onMounted(() => {
  document.addEventListener('click', closeTooltip)
})

onUnmounted(() => {
  document.removeEventListener('click', closeTooltip)
})
</script>

<style scoped>
.gallery-card {
  padding: 16px 18px 18px;
  display: flex;
  flex-direction: column;
  gap: 14px;
  overflow: visible;
}

.gallery-settings {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 4px 2px 6px;
  margin-bottom: 4px;
  border-bottom: 1px solid rgba(226, 232, 240, 0.8);
}

.gallery-settings-actions {
  display: flex;
  justify-content: flex-end;
}

.reset-profile-button {
  border: 1px solid rgba(148, 163, 184, 0.28);
  background: rgba(255, 255, 255, 0.88);
  color: #475569;
  border-radius: 999px;
  padding: 6px 12px;
  font-size: 12px;
  font-weight: 600;
  transition: all 0.15s ease;
}

.reset-profile-button:hover {
  color: #0f172a;
  border-color: rgba(79, 134, 247, 0.28);
  background: #ffffff;
}

.gallery-settings .setting-block {
  gap: 12px;
}

.gallery-toggle-main {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.gallery-title-row,
.setting-title-row {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.gallery-toggle-title,
.gallery-title {
  font-size: 15px;
  font-weight: 700;
  color: #0f172a;
}

.help-icon-wrap {
  position: relative;
  display: inline-flex;
  align-items: center;
  z-index: 12;
  flex-shrink: 0;
  padding: 0;
  border: none;
  background: transparent;
  cursor: pointer;
}

.help-icon {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: 1px solid rgba(148, 163, 184, 0.28);
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.98) 0%, rgba(241, 245, 249, 0.96) 100%);
  color: #5b6b86;
  line-height: 1;
  box-shadow: 0 4px 10px rgba(15, 23, 42, 0.08);
  transition: transform 0.15s ease, box-shadow 0.15s ease, border-color 0.15s ease, color 0.15s ease, background 0.15s ease;
}

.help-icon-wrap:hover .help-icon,
.help-icon-wrap.active .help-icon {
  transform: scale(1.08);
  border-color: rgba(79, 134, 247, 0.45);
  color: #4f86f7;
  background: linear-gradient(180deg, #ffffff 0%, #edf4ff 100%);
  box-shadow: 0 8px 18px rgba(79, 134, 247, 0.16);
}

.help-tooltip {
  position: absolute;
  left: 0;
  top: calc(100% + 12px);
  transform: translateY(-4px);
  width: min(280px, calc(100vw - 48px));
  min-width: 220px;
  padding: 8px 10px;
  border-radius: 10px;
  background: rgba(15, 23, 42, 0.9);
  color: #fff;
  font-size: 12px;
  line-height: 1.5;
  box-shadow: 0 16px 30px rgba(15, 23, 42, 0.2);
  opacity: 0;
  visibility: hidden;
  pointer-events: none;
  transition: opacity 0.15s ease, transform 0.15s ease, visibility 0.15s ease;
  z-index: 40;
  text-align: left;
}

.help-tooltip::before {
  content: '';
  position: absolute;
  left: 10px;
  top: -6px;
  width: 10px;
  height: 10px;
  transform: rotate(45deg);
  background: rgba(15, 23, 42, 0.9);
}

.help-icon-wrap:hover .help-tooltip,
.help-icon-wrap.active .help-tooltip {
  opacity: 1;
  visibility: visible;
  transform: translateY(0);
}

@media (max-width: 520px) {
  .help-tooltip {
    width: min(260px, calc(100vw - 40px));
    min-width: 0;
  }
}

.gallery-toggle-arrow {
  font-size: 18px;
  color: #64748b;
  transition: transform 0.2s ease;
}

.gallery-toggle-arrow.expanded {
  transform: rotate(180deg);
}

.gallery-panel {
  display: flex;
  flex-direction: column;
  gap: 18px;
  padding-top: 6px;
  transform-origin: top center;
}

.gallery-collapse-enter-active,
.gallery-collapse-leave-active {
  transition: opacity 0.24s ease, transform 0.24s ease, max-height 0.28s ease;
  overflow: hidden;
}

.gallery-collapse-enter-from,
.gallery-collapse-leave-to {
  opacity: 0;
  transform: translateY(-8px);
  max-height: 0;
}

.gallery-collapse-enter-to,
.gallery-collapse-leave-from {
  opacity: 1;
  transform: translateY(0);
  max-height: 720px;
}

.gallery-block {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.gallery-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.gallery-head .gallery-title {
  flex-shrink: 0;
}

.gallery-tip {
  font-size: 12px;
  color: #94a3b8;
}

.gallery-grid {
  display: grid;
  gap: 10px;
  align-items: start;
}

.gallery-option-shell {
  width: 100%;
}

.gallery-option-shell--with-caption {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}

.avatar-gallery {
  grid-template-columns: repeat(auto-fit, minmax(64px, 82px));
  justify-content: start;
}

.cover-gallery {
  grid-template-columns: repeat(3, minmax(0, 1fr));
  justify-items: stretch;
}

.gallery-option {
  width: 100%;
  min-width: 0;
  border: 2px solid transparent;
  background: rgba(255, 255, 255, 0.78);
  border-radius: 16px;
  padding: 4px;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.06);
  position: relative;
}

.gallery-option:hover {
  transform: translateY(-1px);
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.1);
}

.gallery-option.active {
  border-color: #4f86f7;
  box-shadow: 0 0 0 3px rgba(79, 134, 247, 0.14);
}

.gallery-option img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  border-radius: 12px;
}

.gallery-file-input {
  display: none;
}

.gallery-option--upload {
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.96) 0%, rgba(239, 246, 255, 0.92) 100%);
  border: 1.5px dashed rgba(79, 134, 247, 0.35);
}

.gallery-option--upload.gallery-option--custom {
  overflow: hidden;
  border-style: solid;
  border-color: rgba(79, 134, 247, 0.24);
}

.gallery-option--upload.gallery-option--custom::after {
  content: attr(data-upload-label);
  position: absolute;
  left: 50%;
  bottom: 10px;
  transform: translateX(-50%);
  font-size: 11px;
  font-weight: 700;
  color: #ffffff;
  letter-spacing: 0.2px;
  z-index: 2;
  pointer-events: none;
  text-shadow: 0 1px 6px rgba(15, 23, 42, 0.35);
}

.upload-option-body {
  width: 100%;
  height: 100%;
  min-height: 48px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  border-radius: 12px;
  color: #4f86f7;
}

.gallery-option--upload.gallery-option--custom .upload-option-body {
  background: transparent;
  color: #ffffff;
  backdrop-filter: blur(1.5px);
  -webkit-backdrop-filter: blur(1.5px);
}

.gallery-option--upload.gallery-option--custom .upload-option-title,
.gallery-option--upload.gallery-option--custom .upload-option-subtitle {
  display: none;
}

.upload-option-icon {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(79, 134, 247, 0.12);
  font-size: 18px;
  font-weight: 500;
}

.gallery-option--upload.gallery-option--custom .upload-option-icon {
  position: relative;
  background: rgba(255, 255, 255, 0.22);
  color: transparent;
}

.gallery-option--upload.gallery-option--custom .upload-option-icon::before {
  content: '↻';
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  font-size: 15px;
  font-weight: 700;
}

.upload-option-title {
  font-size: 11px;
  font-weight: 700;
}

.upload-option-subtitle {
  font-size: 10px;
  color: #7c92b8;
}

.gallery-option--custom {
  border-color: rgba(16, 185, 129, 0.28);
}

.custom-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  padding: 3px 8px;
  border-radius: 999px;
  background: rgba(15, 23, 42, 0.82);
  color: #fff;
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.2px;
}

.avatar-option {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  padding: 3px;
}

.avatar-option.gallery-option--upload {
  padding: 2px;
}

.avatar-option .upload-option-title,
.avatar-option .upload-option-subtitle {
  display: none;
}

.avatar-option.gallery-option--upload .upload-option-body {
  border-radius: 50%;
  min-height: 64px;
  width: 64px;
  height: 64px;
  margin: 0 auto;
  padding: 2px;
}

.avatar-option.gallery-option--upload .upload-option-icon {
  width: 28px;
  height: 28px;
  font-size: 20px;
}

.avatar-option .custom-badge {
  top: -2px;
  right: -6px;
}

.gallery-option-caption {
  display: block;
  width: 100%;
  padding: 0 1px;
  font-size: 11px;
  line-height: 1.35;
  color: #64748b;
  text-align: center;
  white-space: normal;
}

.avatar-option img {
  border-radius: 50%;
}

.cover-option {
  width: 100%;
  aspect-ratio: 16 / 10;
}

.setting-block {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.setting-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.setting-title {
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
}

.setting-value {
  font-size: 13px;
  font-weight: 700;
  color: #4f86f7;
}

.setting-slider {
  width: 100%;
}
</style>
