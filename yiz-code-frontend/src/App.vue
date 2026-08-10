<script setup lang="ts">
import BasicLayout from '@/layouts/BasicLayout.vue'

import { healthcheck } from '@/api/healthController'

healthcheck().then((res) => {
  console.log(res)
})

</script>

<template>
  <BasicLayout />
</template>
