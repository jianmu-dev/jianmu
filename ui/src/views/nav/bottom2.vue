<template>
  <div class="bottom-nav">
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
  padding-top: 10px;
  width: 100%;
  height: 50px;
  line-height: 50px;
  font-size: 12px;
  font-weight: 400;
  color: #595959;
  text-align: center;

  .company, .icp, .email {
    margin-left: 10px;
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