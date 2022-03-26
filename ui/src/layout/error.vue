<template>
  <div class="error-layout">
    <div class="left-top">
      <div class="bg-graph"></div>
      <router-link to="/">
        <div class="logo"></div>
      </router-link>
    </div>
    <router-view/>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';

export default defineComponent({});
</script>

<style scoped lang="less">
.error-layout {
  .left-top {
    width: 478px;
    height: 197px;
    position: fixed;
    left: 0;
    top: 0;

    .bg-graph {
      width: 478px;
      height: 300px;
      position: absolute;
      left: -145px;
      top: -200px;
      transform: rotate(27deg);
      background-color: #D9EBFF;
      border-bottom-right-radius: 111px;
      opacity: 0.85;
    }

    .logo {
      position: absolute;
      left: 110px;
      top: 55px;
      width: 150px;
      height: 34px;
      background-image: url('@/assets/svgs/logo/main.svg');
      background-repeat: no-repeat;
      background-size: contain;
    }
  }
}
</style>