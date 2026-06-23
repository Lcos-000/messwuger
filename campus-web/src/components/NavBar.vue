<template>
  <nav class="nav-bar">
    <router-link to="/schedule" class="nav-item" active-class="active">
      <div class="nav-icon-wrap">
        <svg class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <rect x="3" y="4" width="18" height="18" rx="2"/>
          <line x1="16" y1="2" x2="16" y2="6"/>
          <line x1="8" y1="2" x2="8" y2="6"/>
          <line x1="3" y1="10" x2="21" y2="10"/>
        </svg>
      </div>
      <span>课表</span>
    </router-link>
    <router-link to="/profile" class="nav-item" active-class="active">
      <div class="nav-icon-wrap">
        <svg class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
          <circle cx="12" cy="7" r="4"/>
        </svg>
      </div>
      <span>我的</span>
    </router-link>
  </nav>
</template>

<script setup>
</script>

<style scoped>
.nav-bar {
  display: flex;
  background: #fff;
  border-top: 1px solid #e8edf5;
  box-shadow: 0 -2px 16px rgba(15,25,50,.06);
  z-index: 100;
  flex-shrink: 0;
}

.nav-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 10px 0 8px;
  color: #b0bdd4;
  transition: color .2s;
  gap: 3px;
  position: relative;
}

.nav-icon-wrap {
  width: 40px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  transition: background .2s cubic-bezier(0.25, 0.1, 0.25, 1), transform .2s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.nav-icon {
  width: 22px;
  height: 22px;
  transition: transform .2s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.nav-item span {
  font-size: 11px;
  font-weight: 500;
  letter-spacing: .3px;
  transition: color .2s cubic-bezier(0.25, 0.1, 0.25, 1);
}

.nav-item.active {
  color: #4f86f7;
}
.nav-item.active .nav-icon-wrap {
  background: #eef3ff;
  transform: scale(1.08);
}
.nav-item.active .nav-icon {
  transform: scale(1.05);
}

/* Mobile bottom bar */
@media screen and (max-width: 767px) {
  .nav-bar {
    width: 100%;
    height: calc(58px + env(safe-area-inset-bottom, 0px));
    padding-bottom: env(safe-area-inset-bottom, 0px);
  }
}

/* Desktop left sidebar */
@media screen and (min-width: 768px) {
  .nav-bar {
    flex-direction: column;
    width: 72px;
    height: 100vh;
    border-top: none;
    border-right: 1px solid #e8edf5;
    box-shadow: 2px 0 16px rgba(15,25,50,.06);
    padding-top: 16px;
    gap: 4px;
  }
  .nav-item {
    flex: none;
    padding: 14px 0;
  }
}
</style>
