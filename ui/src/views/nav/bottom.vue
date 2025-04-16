<template>
  <div v-if="true"></div>
  <div v-else class="bottom-nav">
    <a href="https://portal.mulanos.cn" target="_blank" class="mulan">{{ $t('bottom.project') }}</a>
    <a href="https://jianmu.dev" target="_blank"> {{ $t('bottom.about') }}</a>
    <a href="https://v2.jianmu.dev" target="_blank">{{ $t('bottom.manual') }}</a>
    <a href="mailto:support@jianmu.dev">{{ $t('bottom.contact') }}</a>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';

export default defineComponent({
  setup() {
    return {
      currentYear: new Date().getFullYear(),
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
  font-size: 14px;
  font-weight: 400;
  background-color: #082340;
  color: #ffffff;
  text-align: center;

  > :nth-child(n + 2) {
    margin-left: 30px;
  }

  .mulan {
    line-height: 24px;
    padding-left: 30px;
    background-image: url('@/assets/svgs/logo/mulan.svg');
    background-repeat: no-repeat;
    background-size: 24px 24px;
  }
}
</style>
