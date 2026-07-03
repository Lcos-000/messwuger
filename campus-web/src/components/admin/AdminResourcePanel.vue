<template>
  <div v-if="loading" class="admin-state">{{ adminConfig.TEXT.LOADING_TEXT }}</div>
  <div v-else-if="resourceCards.length === 0" class="admin-state">{{ adminConfig.TEXT.EMPTY_TEXT }}</div>
  <div v-else class="resource-panel">
    <a
      v-for="item in resourceCards"
      :key="item.code || `${item.name}-${item.url}`"
      class="resource-link"
      :href="item.url"
      target="_blank"
      rel="noopener noreferrer"
    >
      <span class="resource-text">{{ item.name }}{{ adminConfig.TEXT.RESOURCE_SEPARATOR }}</span>
      <span class="resource-url">{{ item.url }}</span>
    </a>
  </div>
</template>

<script setup>
defineProps({
  adminConfig: {
    type: Object,
    required: true
  },
  loading: {
    type: Boolean,
    default: false
  },
  resourceCards: {
    type: Array,
    default: () => []
  }
})
</script>

<style scoped>
.admin-state,
.resource-panel {
  min-height: 280px;
  border: 1px solid var(--admin-panel-border);
  border-radius: 10px;
  background: var(--admin-panel-bg);
}

.admin-state {
  display: grid;
  place-items: center;
  color: var(--admin-text-secondary);
}

.resource-panel {
  display: flex;
  flex-direction: column;
  width: min(var(--admin-panel-max-width), 100%);
  padding: 8px 24px;
}

.resource-link {
  display: flex;
  align-items: baseline;
  gap: 0;
  padding: 18px 0;
  border-bottom: 1px solid var(--admin-divider);
  color: var(--admin-text-link);
  text-decoration: underline;
  text-decoration-color: var(--admin-text-link-muted);
  text-underline-offset: 4px;
}

.resource-link:last-child {
  border-bottom: none;
}

.resource-link:hover {
  color: var(--admin-text-link-hover);
  text-decoration-color: var(--admin-text-link-hover);
}

.resource-link:hover .resource-text,
.resource-link:hover .resource-url {
  color: var(--admin-text-link-hover);
}

.resource-text {
  flex: 0 0 auto;
  font-size: var(--admin-resource-name-size);
  font-weight: 500;
  color: var(--admin-text-link-strong);
  transition: color 0.18s ease;
}

.resource-url {
  flex: 1 1 auto;
  font-size: var(--admin-resource-url-size);
  color: var(--admin-text-link-muted);
  line-height: 1.5;
  word-break: break-all;
  transition: color 0.18s ease;
}

@media screen and (max-width: 960px) {
  .resource-link {
    flex-direction: column;
    align-items: flex-start;
    gap: 6px;
  }

  .resource-text {
    font-size: var(--admin-mobile-resource-name-size);
  }

  .resource-url {
    font-size: var(--admin-mobile-resource-url-size);
  }
}
</style>
