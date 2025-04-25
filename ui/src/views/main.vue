<template>
  <div class="main">
    <div>
      <router-link to="/component-lib">{{ $t('main.componentLib') }}</router-link>
    </div>
    <div>
      <router-link to="/login">{{ $t('main.loginPage') }}</router-link>
    </div>
    <div class="error">
      <div>{{ $t('main.errorPage') }}</div>
      <div>
        <div>
          <router-link to="/error/browser-version">{{ $t('main.browserVersion') }}</router-link>
        </div>
        <div>
          <router-link to="/error/http-status/404">404</router-link>
        </div>
        <div>
          <router-link to="/error/http-status/500">500</router-link>
        </div>
        <div>
          <router-link to="/error/network">{{ $t('main.network') }}</router-link>
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
