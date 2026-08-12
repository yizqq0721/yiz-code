<script setup lang="ts">
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import logo from '@/assets/logo.svg'

defineOptions({ name: 'GlobalHeader' })

export interface MenuItem {
  key: string
  label: string
  path: string
}

const props = withDefaults(
  defineProps<{
    menuItems?: MenuItem[]
    title?: string
  }>(),
  {
    menuItems: () => [],
    title: 'YiZ Code',
  },
)

const router = useRouter()
const route = useRoute()

const selectedKeys = computed<string[]>(() => [route.path])

function handleMenuClick(info: { key: string }) {
  router.push(info.key)
}
</script>

<template>
  <a-layout-header class="global-header">
    <div class="header-left">
      <router-link to="/" class="logo-area">
        <img :src="logo" alt="logo" class="logo-img" />
        <span class="site-title">{{ props.title }}</span>
      </router-link>
    </div>

    <a-menu
      v-model:selectedKeys="selectedKeys"
      mode="horizontal"
      :items="props.menuItems.map((item) => ({ key: item.path, label: item.label }))"
      class="header-menu"
      @click="handleMenuClick"
    />

    <div class="header-right">
      <a-button type="primary">登录</a-button>
    </div>
  </a-layout-header>
</template>

<style scoped>
.global-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  position: sticky;
  top: 0;
  z-index: 100;
  height: 64px;
  line-height: 64px;
}

.header-left {
  flex-shrink: 0;
}

.logo-area {
  display: flex;
  align-items: center;
  text-decoration: none;
  color: inherit;
}

.logo-img {
  height: 32px;
  width: auto;
  margin-right: 12px;
}

.site-title {
  font-size: 18px;
  font-weight: 600;
  white-space: nowrap;
}

.header-menu {
  flex: 1;
  border-bottom: none;
  line-height: 64px;
  margin: 0 24px;
  min-width: 0;
}

.header-right {
  flex-shrink: 0;
  display: flex;
  align-items: center;
}

@media (max-width: 768px) {
  .global-header {
    padding: 0 12px;
    flex-wrap: wrap;
    height: auto;
    line-height: normal;
  }

  .header-menu {
    order: 3;
    width: 100%;
    margin: 0;
    line-height: 46px;
  }

  .site-title {
    font-size: 16px;
  }
}
</style>
