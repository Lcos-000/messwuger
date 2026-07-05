<template>
  <div class="login-container" :style="loginRootStyle">
    <div class="login-background" aria-hidden="true">
      <div class="login-gradient-orb login-gradient-orb--one"></div>
      <div class="login-gradient-orb login-gradient-orb--two"></div>
      <div class="login-gradient-orb login-gradient-orb--three"></div>
      <div class="login-grid-glow"></div>
    </div>

    <div class="login-content">
      <section class="login-brand" aria-label="系统标题">
        <span v-if="brand.KICKER" class="brand-kicker">{{ brand.KICKER }}</span>
        <h1 class="brand-title">{{ brand.TITLE }}</h1>
        <p v-if="brand.SUBTITLE" class="brand-subtitle">{{ brand.SUBTITLE }}</p>
      </section>

      <div class="login-box">
        <div class="tabs">
          <button
            type="button"
            class="tab"
            :class="{ active: isLoginTab }"
            @click="isLoginTab = true"
          >{{ formText.LOGIN_TAB_TEXT }}</button>
          <button
            type="button"
            class="tab"
            :class="{ active: !isLoginTab }"
            @click="isLoginTab = false"
          >{{ formText.REGISTER_TAB_TEXT }}</button>
        </div>

        <form @submit.prevent="handleSubmit" class="form">
          <div class="form-item">
            <label class="input-label">{{ formText.STUDENT_ID_LABEL }}</label>
            <input
              v-model.trim="formData.studentId"
              type="text"
              :placeholder="formText.STUDENT_ID_PLACEHOLDER"
              required
            />
          </div>
          <div class="form-item">
            <label class="input-label">{{ formText.PASSWORD_LABEL }}</label>
            <input
              v-model="formData.password"
              type="password"
              :placeholder="formText.PASSWORD_PLACEHOLDER"
              required
            />
          </div>
          <button type="submit" class="submit-btn" :disabled="loading">
            {{ submitText }}
          </button>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { adminLogin, login, register } from '@/api/index'
import { ADMIN_CONFIG, APP_CONFIG, HTTP_STATUS, LOGIN_VIEW_CONFIG, ROUTE_PATHS } from '@/config'
import { persistLoginState } from '@/utils/auth'

const router = useRouter()
const isLoginTab = ref(true)
const loading = ref(false)

const brand = LOGIN_VIEW_CONFIG.BRAND
const formText = LOGIN_VIEW_CONFIG.FORM
const loginRootStyle = LOGIN_VIEW_CONFIG.CSS_VARIABLES

const formData = reactive({
  studentId: '',
  password: ''
})

const submitText = computed(() => {
  if (loading.value) return formText.LOADING_TEXT
  return isLoginTab.value ? formText.LOGIN_SUBMIT_TEXT : formText.REGISTER_SUBMIT_TEXT
})

const buildLoginPayload = () => {
  const rawStudentId = formData.studentId.trim()
  const adminMode = rawStudentId.endsWith(ADMIN_CONFIG.LOGIN_SUFFIX)
  const studentId = adminMode
    ? rawStudentId.slice(0, -ADMIN_CONFIG.LOGIN_SUFFIX.length).trim()
    : rawStudentId

  return {
    adminMode,
    payload: {
      studentId,
      password: formData.password
    }
  }
}

const redirectAfterLogin = (adminMode) => {
  router.push(adminMode ? ROUTE_PATHS.ADMIN : ROUTE_PATHS.SCHEDULE)
}

const handleSubmit = async () => {
  if (!formData.studentId || !formData.password) {
    alert(APP_CONFIG.FORM_INCOMPLETE_TIP)
    return
  }

  const { adminMode, payload } = buildLoginPayload()
  if (!payload.studentId) {
    alert(APP_CONFIG.FORM_INCOMPLETE_TIP)
    return
  }

  loading.value = true
  try {
    if (isLoginTab.value) {
      const requestFn = adminMode ? adminLogin : login
      const res = await requestFn(payload)
      if (res.code === HTTP_STATUS.SUCCESS && res.data) {
        persistLoginState(res.data, adminMode)
        redirectAfterLogin(adminMode)
      }
    } else {
      const res = await register(payload)
      if (res.code === HTTP_STATUS.SUCCESS) {
        const loginRes = await login(payload)
        if (loginRes.code === HTTP_STATUS.SUCCESS && loginRes.data) {
          persistLoginState(loginRes.data, false)
          redirectAfterLogin(false)
        }
      }
    }
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  position: relative;
  min-height: var(--login-page-min-height);
  overflow: hidden;
  background: var(--login-container-bg);
}

.login-background {
  position: absolute;
  inset: 0;
  overflow: hidden;
  pointer-events: none;
}

.login-background::before {
  content: '';
  position: absolute;
  inset: 0;
  background: var(--login-before-bg);
}

.login-background::after {
  content: '';
  position: absolute;
  inset: 0;
  background: var(--login-after-bg);
}

.login-gradient-orb {
  position: absolute;
  border-radius: var(--login-orb-radius);
  filter: blur(var(--login-orb-blur));
  opacity: var(--login-orb-opacity);
  will-change: transform;
  animation: floatOrb var(--login-orb-duration) ease-in-out infinite;
}

.login-gradient-orb--one {
  width: var(--login-orb-one-size);
  height: var(--login-orb-one-size);
  top: var(--login-orb-one-top);
  left: var(--login-orb-one-left);
  background: var(--login-orb-one-bg);
}

.login-gradient-orb--two {
  width: var(--login-orb-two-size);
  height: var(--login-orb-two-size);
  right: var(--login-orb-two-right);
  top: var(--login-orb-two-top);
  background: var(--login-orb-two-bg);
  animation-delay: var(--login-orb-two-delay);
}

.login-gradient-orb--three {
  width: var(--login-orb-three-size);
  height: var(--login-orb-three-size);
  left: var(--login-orb-three-left);
  bottom: var(--login-orb-three-bottom);
  background: var(--login-orb-three-bg);
  animation-delay: var(--login-orb-three-delay);
}

.login-grid-glow {
  position: absolute;
  inset: 0;
  background-image: var(--login-grid-bg);
  background-size: var(--login-grid-size);
  mask-image: var(--login-grid-mask);
  opacity: var(--login-grid-opacity-min);
  animation: gridBreath var(--login-grid-duration) ease-in-out infinite;
}

.login-content {
  position: relative;
  z-index: var(--login-content-z-index);
  min-height: var(--login-page-min-height);
  width: min(var(--login-content-max-width), 100%);
  margin: 0 auto;
  display: grid;
  grid-template-columns: var(--login-content-columns);
  align-items: var(--login-content-align-items);
  gap: var(--login-content-gap);
  padding: var(--login-content-padding);
}

.login-brand {
  position: relative;
  max-width: var(--login-brand-max-width);
  padding-left: var(--login-brand-padding-left);
  color: var(--login-brand-color);
}

.login-brand::before {
  content: '';
  position: absolute;
  top: var(--login-brand-stripe-top);
  left: 0;
  width: var(--login-brand-stripe-width);
  height: var(--login-brand-stripe-height);
  border-radius: var(--login-brand-stripe-radius);
  background: var(--login-brand-stripe-bg);
  box-shadow: var(--login-brand-stripe-shadow);
}

.brand-kicker {
  display: inline-flex;
  align-items: center;
  min-height: var(--login-brand-kicker-min-height);
  padding: var(--login-brand-kicker-padding);
  border-radius: var(--login-brand-kicker-radius);
  background: var(--login-brand-kicker-bg);
  border: var(--login-brand-kicker-border);
  color: var(--login-brand-kicker-color);
  font-size: var(--login-brand-kicker-size);
  font-weight: var(--login-brand-kicker-weight);
  letter-spacing: var(--login-brand-kicker-letter-spacing);
  text-transform: uppercase;
}

.brand-title {
  margin-top: var(--login-brand-title-margin-top);
  font-size: var(--login-brand-title-size);
  line-height: var(--login-brand-title-line-height);
  font-weight: var(--login-brand-title-weight);
  letter-spacing: var(--login-brand-title-letter-spacing);
}

.brand-subtitle {
  margin-top: var(--login-brand-subtitle-margin-top);
  max-width: var(--login-brand-subtitle-max-width);
  font-size: var(--login-brand-subtitle-size);
  line-height: var(--login-brand-subtitle-line-height);
  color: var(--login-brand-subtitle-color);
}

.login-box {
  width: var(--login-box-width);
  padding: var(--login-box-padding);
  border-radius: var(--login-box-radius);
  background: var(--login-box-bg);
  border: var(--login-box-border);
  box-shadow: var(--login-box-shadow);
  backdrop-filter: blur(var(--login-box-blur));
  -webkit-backdrop-filter: blur(var(--login-box-blur));
}

.tabs {
  display: flex;
  gap: var(--login-tabs-gap);
  padding: var(--login-tabs-padding);
  margin-bottom: var(--login-tabs-margin-bottom);
  border-radius: var(--login-tabs-radius);
  background: var(--login-tabs-bg);
}

.tab {
  flex: 1;
  min-height: var(--login-tab-min-height);
  border-radius: var(--login-tab-radius);
  font-size: var(--login-tab-size);
  font-weight: var(--login-tab-weight);
  color: var(--login-tab-color);
  background: transparent;
  transition: var(--login-tab-transition);
}

.tab.active {
  color: var(--login-tab-active-color);
  background: var(--login-tab-active-bg);
  box-shadow: var(--login-tab-active-shadow);
}

.form {
  display: flex;
  flex-direction: column;
  gap: var(--login-form-gap);
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: var(--login-form-item-gap);
}

.input-label {
  font-size: var(--login-label-size);
  font-weight: var(--login-label-weight);
  color: var(--login-label-color);
}

.form-item input {
  width: 100%;
  min-height: var(--login-input-min-height);
  padding: var(--login-input-padding);
  border: var(--login-input-border);
  border-radius: var(--login-input-radius);
  font-size: var(--login-input-size);
  color: var(--login-input-text-color);
  background: var(--login-input-bg);
  outline: none;
  transition: var(--login-input-transition);
}

.form-item input::placeholder {
  color: var(--login-input-placeholder-color);
}

.form-item input:focus {
  border-color: var(--login-input-focus-border);
  box-shadow: var(--login-input-focus-ring);
  background: var(--login-input-focus-bg);
}

.submit-btn {
  width: 100%;
  min-height: var(--login-submit-min-height);
  margin-top: var(--login-submit-margin-top);
  border-radius: var(--login-submit-radius);
  font-size: var(--login-submit-size);
  font-weight: var(--login-submit-weight);
  letter-spacing: var(--login-submit-letter-spacing);
  color: var(--login-submit-color);
  background: var(--login-submit-bg);
  box-shadow: var(--login-submit-shadow);
  transition: var(--login-submit-transition);
}

.submit-btn:hover:not(:disabled) {
  transform: var(--login-submit-hover-transform);
  box-shadow: var(--login-submit-shadow-hover);
}

.submit-btn:active:not(:disabled) {
  transform: translateY(0);
}

.submit-btn:disabled {
  cursor: not-allowed;
  filter: var(--login-submit-disabled-filter);
  opacity: var(--login-submit-disabled-opacity);
}

@keyframes floatOrb {
  0%,
  100% {
    transform: translate3d(0, 0, 0) scale(1);
  }
  33% {
    transform: translate3d(var(--login-orb-move-x-positive), var(--login-orb-move-y-negative), 0) scale(var(--login-orb-scale-up));
  }
  66% {
    transform: translate3d(var(--login-orb-move-x-negative), var(--login-orb-move-y-positive), 0) scale(var(--login-orb-scale-down));
  }
}

@keyframes gridBreath {
  0%,
  100% {
    opacity: var(--login-grid-opacity-min);
  }
  50% {
    opacity: var(--login-grid-opacity-max);
  }
}

@media (max-width: 980px) {
  .login-content {
    width: min(var(--login-tablet-content-width), 100%);
    grid-template-columns: 1fr;
    gap: var(--login-tablet-content-gap);
    padding: var(--login-tablet-content-padding);
  }

  .login-brand {
    max-width: none;
    padding-left: var(--login-tablet-brand-padding-left);
  }

  .brand-title {
    font-size: var(--login-tablet-brand-title-size);
  }
}

@media (max-width: 640px) {
  .login-container {
    background: var(--login-container-bg-mobile);
  }

  .login-gradient-orb--one {
    width: var(--login-mobile-orb-one-size);
    height: var(--login-mobile-orb-one-size);
    top: var(--login-mobile-orb-one-top);
    left: var(--login-mobile-orb-one-left);
  }

  .login-gradient-orb--two {
    width: var(--login-mobile-orb-two-size);
    height: var(--login-mobile-orb-two-size);
    top: var(--login-mobile-orb-two-top);
    right: var(--login-mobile-orb-two-right);
  }

  .login-gradient-orb--three {
    width: var(--login-mobile-orb-three-size);
    height: var(--login-mobile-orb-three-size);
    left: var(--login-mobile-orb-three-left);
    bottom: var(--login-mobile-orb-three-bottom);
  }

  .login-content {
    gap: var(--login-mobile-content-gap);
    padding: var(--login-mobile-content-padding);
  }

  .login-brand {
    padding-left: var(--login-mobile-brand-padding-left);
  }

  .login-brand::before {
    height: var(--login-mobile-brand-stripe-height);
  }

  .brand-title {
    margin-top: var(--login-mobile-brand-title-margin-top);
    font-size: var(--login-mobile-brand-title-size);
  }

  .brand-subtitle {
    font-size: var(--login-mobile-brand-subtitle-size);
    line-height: var(--login-mobile-brand-subtitle-line-height);
  }

  .login-box {
    padding: var(--login-mobile-box-padding);
    border-radius: var(--login-mobile-box-radius);
  }

  .tabs {
    margin-bottom: var(--login-mobile-tabs-margin-bottom);
  }

  .tab {
    min-height: var(--login-mobile-tab-min-height);
    font-size: var(--login-mobile-tab-size);
  }

  .form-item input,
  .submit-btn {
    min-height: var(--login-mobile-input-min-height);
  }
}
</style>
