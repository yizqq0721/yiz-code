<script setup lang="ts">
import BasicLayout from '@/layouts/BasicLayout.vue'
import { useLoginUserStore } from '@/stores/LoginUser'

const loginUserState = useLoginUserStore();
loginUserState.fetchLoginUser().then((res) => {
  console.log('登录用户信息', res);
});

</script>

<template>
  <BasicLayout />
</template>
