<template>
  <div class="login-container">
    <div class="login-box">
      <h1 class="title">{{ APP_CONFIG.APP_TITLE }}</h1>
      
      <div class="tabs">
        <div 
          class="tab" 
          :class="{ active: isLoginTab }" 
          @click="isLoginTab = true"
        >登录</div>
        <div 
          class="tab" 
          :class="{ active: !isLoginTab }" 
          @click="isLoginTab = false"
        >注册</div>
      </div>

      <form @submit.prevent="handleSubmit" class="form">
        <div class="form-item">
          <input 
            type="text" 
            v-model="formData.studentId" 
            placeholder="请输入学号" 
            required
          />
        </div>
        <div class="form-item">
          <input 
            type="password" 
            v-model="formData.password" 
            placeholder="请输入密码" 
            required
          />
        </div>
        <button type="submit" class="submit-btn" :disabled="loading">
          {{ loading ? '处理中...' : (isLoginTab ? '登 录' : '注 册') }}
        </button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { login, register } from '@/api/index'
import { APP_CONFIG, HTTP_STATUS, ROUTE_PATHS, STORAGE_KEYS } from '@/config'

const router = useRouter()
const isLoginTab = ref(true)
const loading = ref(false)

const formData = reactive({
  studentId: '',
  password: ''
})

const handleSubmit = async () => {
  if (!formData.studentId || !formData.password) {
    alert(APP_CONFIG.FORM_INCOMPLETE_TIP)
    return
  }

  loading.value = true
  try {
    if (isLoginTab.value) {
      // 登录
      const res = await login(formData)
      if (res.code === HTTP_STATUS.SUCCESS && res.data) {
        localStorage.setItem(STORAGE_KEYS.TOKEN, res.data)
        router.push(ROUTE_PATHS.SCHEDULE)
      }
    } else {
      // 注册
      const res = await register(formData)
      if (res.code === HTTP_STATUS.SUCCESS) {
        const loginRes = await login(formData)
        if (loginRes.code === HTTP_STATUS.SUCCESS && loginRes.data) {
          localStorage.setItem(STORAGE_KEYS.TOKEN, loginRes.data)
          router.push(ROUTE_PATHS.SCHEDULE)
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
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #4f86f7 0%, #a5c8ff 100%);
  padding: 20px;
}

.login-box {
  background: var(--white);
  padding: 40px 30px;
  border-radius: 12px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 400px;
}

.title {
  text-align: center;
  color: var(--primary-color-dark);
  font-size: 24px;
  margin-bottom: 30px;
}

.tabs {
  display: flex;
  margin-bottom: 25px;
  border-bottom: 2px solid var(--border-color);
}

.tab {
  flex: 1;
  text-align: center;
  padding: 10px 0;
  font-size: 16px;
  cursor: pointer;
  color: var(--text-color-light);
  transition: all 0.3s;
  position: relative;
}

.tab.active {
  color: var(--primary-color);
  font-weight: bold;
}

.tab.active::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 100%;
  height: 2px;
  background-color: var(--primary-color);
}

.form-item {
  margin-bottom: 20px;
}

.form-item input {
  width: 100%;
  padding: 12px 15px;
  border: 1px solid var(--border-color);
  border-radius: 6px;
  font-size: 16px;
  outline: none;
  transition: border-color 0.3s;
}

.form-item input:focus {
  border-color: var(--primary-color);
}

.submit-btn {
  width: 100%;
  padding: 12px;
  background-color: var(--primary-color);
  color: var(--white);
  border-radius: 6px;
  font-size: 16px;
  font-weight: bold;
  transition: background-color 0.3s;
}

.submit-btn:hover:not(:disabled) {
  background-color: var(--primary-color-dark);
}

.submit-btn:disabled {
  background-color: #a5d6a7;
  cursor: not-allowed;
}
</style>


