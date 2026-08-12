<script setup lang="ts">
import { RouterView } from 'vue-router'
import GlobalHeader from '@/components/GlobalHeader.vue'
import GlobalFooter from '@/components/GlobalFooter.vue'
import type { MenuItem } from '@/components/GlobalHeader.vue'

defineOptions({ name: 'BasicLayout' })

const menuItems: MenuItem[] = [
  { key: 'home', label: '首页', path: '/' },
  { key: 'about', label: '关于', path: '/about' },
]
</script>

<template>
  <a-layout class="basic-layout">
    <GlobalHeader :menu-items="menuItems" title="YiZ Code" />
    <a-layout-content class="layout-content">
      <RouterView />
    </a-layout-content>
    <GlobalFooter />
  </a-layout>
</template>

<style scoped>
.basic-layout {
  min-height: 100vh;
}

.layout-content {
  padding: 24px;
  background: var(--color-background, #fff);
}
</style>
