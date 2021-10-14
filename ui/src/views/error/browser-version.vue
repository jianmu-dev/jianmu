<template>
  <div class="browser-version-error">
    <div class="desc">抱歉，不支持此浏览器</div>
    <div class="back">
      <router-link to="/">
        <jm-button type="primary" class="jm-icon-button-back" size="small">返回首页</jm-button>
      </router-link>
    </div>
    <div class="versions">
      <div v-for="{name, minVersion} in browsers" :key="name" :class="{version: true, [name]: true}">
        {{ minVersion }}
      </div>
    </div>
    <bottom-nav/>
  </div>
  <div class="right-bottom">
    <div class="bg-graph"></div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import BottomNav from '@/views/nav/bottom.vue';

// TODO 待完善最低版
// 下面版本为css3的transition属性支持情况（除edge）
const browsers: {
  name: string;
  minVersion: string;
}[] = [{
  name: 'chrome',
  minVersion: '26.0',
}, {
  name: 'edge',
  minVersion: '45.0',
}, {
  name: 'firefox',
  minVersion: '16.0',
}, {
  name: 'safari',
  minVersion: '6.1',
}, {
  name: 'opera',
  minVersion: '12.1',
}];

export default defineComponent({
  components: { BottomNav },
  setup() {
    return {
      browsers,
    };
  },
});
</script>

<style scoped lang="less">
.browser-version-error {
  margin-top: 16vh;
  padding-top: 242px;
  background-image: url('@/assets/svgs/error/browser.svg');
  background-repeat: no-repeat;
  background-position: center top;
  text-align: center;

  .desc {
    padding-top: 15px;
    font-size: 14px;
    font-weight: 400;
    color: #595959;
  }

  .back {
    padding-top: 50px;
  }

  .versions {
    position: fixed;
    left: 0;
    bottom: 60px;
    display: flex;
    justify-content: center;
    align-items: center;
    padding-top: 20px;
    width: 100%;
    height: 150px;
    background-color: #082340;

    .version {
      padding-top: 80px;
      width: 150px;
      text-align: center;
      font-size: 14px;
      color: #FFFFFF;
      background-repeat: no-repeat;
      background-position: center top;

      &.chrome {
        background-image: url('@/assets/images/error/browser-icon/chrome.png');
      }

      &.edge {
        background-image: url('@/assets/images/error/browser-icon/edge.png');
      }

      &.firefox {
        background-image: url('@/assets/images/error/browser-icon/firefox.png');
      }

      &.opera {
        background-image: url('@/assets/images/error/browser-icon/opera.png');
      }

      &.safari {
        background-image: url('@/assets/images/error/browser-icon/safari.png');
      }
    }
  }
}

.right-bottom {
  position: fixed;
  right: 180px;
  bottom: -266px;
  width: 152px;
  height: 300px;

  .bg-graph {
    width: 500px;
    height: 280px;
    position: absolute;
    background-color: #D9EBFF;
    border-top-left-radius: 111px;
    opacity: 0.14;
    transform: rotate(23deg);
  }
}
</style>
