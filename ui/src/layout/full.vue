<template>
 <div class="full">
   <router-view></router-view>
 </div>
</template>

<script lang='ts'>
import { defineComponent } from 'vue';

export default defineComponent({
  name: 'full',
  setup() {
    return {};
  },
});
</script>

<style scoped lang='less'>
</style>
