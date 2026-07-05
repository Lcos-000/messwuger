import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import './assets/base.css'
import './assets/main.css'
import { loadAndApplyGlobalFontPreference } from '@/utils/globalFont'

loadAndApplyGlobalFontPreference()

const app = createApp(App)
app.use(router)
app.mount('#app')
