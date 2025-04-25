<template>
  <div class="network-error">
    <div class="title">{{ $t('network.title') }}</div>
    <div class="desc">{{ $t('network.desc') }}</div>
    <div class="back">
      <router-link to="/">
        <jm-button type="primary" class="jm-icon-button-back" size="small">{{ $t('network.backHome') }}</jm-button>
      </router-link>
    </div>
    <bottom-nav />
  </div>
  <div class="right-bottom">
    <div class="bg-graph"></div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import BottomNav from '@/views/nav/bottom.vue';

export default defineComponent({
  components: { BottomNav },
});
</script>

<style scoped lang="less">
.network-error {
  margin-top: 16vh;
  padding-top: 279px;
  background-image: url('@/assets/svgs/error/netless.svg');
  background-repeat: no-repeat;
  background-position: center top;
  text-align: center;

  .title {
    padding-top: 30px;
    font-size: 22px;
    font-weight: 500;
    color: #333333;
    line-height: 37px;
  }

  .desc {
    padding-top: 10px;
    font-size: 14px;
    font-weight: 400;
    color: #595959;
  }

  .back {
    padding-top: 50px;
  }
}

.right-bottom {
  position: fixed;
  right: 0;
  bottom: 132px;
  width: 152px;
  height: 300px;

  .bg-graph {
    width: 500px;
    height: 280px;
    position: absolute;
    background-color: #d9ebff;
    border-top-left-radius: 111px;
    transform: rotate(-67deg);
  }
}
</style>
