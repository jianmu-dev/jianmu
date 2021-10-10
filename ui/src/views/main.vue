<template>
  <div class="main">
    <div>
      <router-link to="/component-lib">组件库</router-link>
    </div>
    <div>
      <router-link to="/login">登录页</router-link>
    </div>
    <div class="error">
      <div>错误页：</div>
      <div>
        <div>
          <router-link to="/error/browser-version">浏览器版本</router-link>
        </div>
        <div>
          <router-link to="/error/http-status/404">404</router-link>
        </div>
        <div>
          <router-link to="/error/http-status/500">500</router-link>
        </div>
        <div>
          <router-link to="/error/network">网络</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';

export default defineComponent({});
</script>

<style scoped lang="less">
.main {
  margin: 50px auto;
  line-height: 50px;
  width: 200px;

  .error {
    display: flex;
  }
}
</style>