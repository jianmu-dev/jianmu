<template>
  <div class="bottom-nav">
    <a href="https://portal.mulanos.cn" target="_blank">木兰社区孵化项目</a>
    <a href="https://jianmu.dev" target="_blank">关于建木</a>
    <a href="https://docs.jianmu.dev" target="_blank">使用手册</a>
    <a href="mailto:support@jianmu.dev">联系我们</a>
    <a v-if="icpVisible" href="https://beian.miit.gov.cn" target="_blank">浙ICP备12032350号-5</a>
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

  > :nth-child(n+2) {
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