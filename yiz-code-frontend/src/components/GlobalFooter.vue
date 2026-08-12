<script setup lang="ts">
defineOptions({ name: 'GlobalFooter' })
</script>

<template>
  <a-layout-footer class="global-footer">
    <a-space>
      <span>GitHub</span>
      <a href="https://github.com/yizqq0721" target="_blank" rel="noopener noreferrer">yizqq</a>
    </a-space>
  </a-layout-footer>
</template>

<style scoped>
.global-footer {
  text-align: center;
  padding: 16px 50px;
  color: var(--color-text, #666);
  background: var(--color-background-soft, #f5f5f5);
}

.global-footer a {
  color: var(--color-text, #666);
  text-decoration: none;
}

.global-footer a:hover {
  color: hsla(160, 100%, 37%, 1);
}
</style>
