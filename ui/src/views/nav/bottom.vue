<template>
  <div v-if="true"></div>
  <div v-else class="bottom-nav">
    <span class="copyright">2020-{{ currentYear }}</span>
    <a href="https://jianmu.dev" target="_blank" class="company">jianmu.dev</a>
    <a v-if="icpVisible" href="http://beian.miit.gov.cn/" target="_blank" class="icp">浙ICP备12032350号-5</a>
    <span class="email">邮箱：<a href="mailto:support@jianmu.dev">support@jianmu.dev</a></span>
    <span class="email">商务合作：<a href="mailto:bd@jianmu.dev">bd@jianmu.dev</a></span>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';

export default defineComponent({
  setup() {
    return {
      currentYear: new Date().getFullYear(),
      icpVisible: import.meta.env.mode === 'cdn',
    };
  },
});
</script>

<style scoped lang="less">
.bottom-nav {
  position: absolute;
  left: 0;
  bottom: 0;
  width: 100%;
  height: 60px;
  line-height: 60px;
  font-size: 12px;
  font-weight: 400;
  background-color: #082340;
  color: #FFFFFF;
  text-align: center;

  .company, .icp, .email {
    margin-left: 30px;
  }

  a {
    color: inherit;
    text-decoration: none;

    &:hover {
      text-decoration: underline;
    }
  }
}
</style>