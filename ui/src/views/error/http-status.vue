<template>
  <div :class="{'http-status-error': true, [`_${status}`]: true}">
    <div class="desc">{{ message }}</div>
    <div class="back">
      <router-link to="/">
        <jm-button type="primary" class="jm-icon-button-back" size="small">返回首页</jm-button>
      </router-link>
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

export default defineComponent({
  components: { BottomNav },
  props: {
    value: {
      type: String,
      required: true,
    },
    errMessage: {
      type: String,
      default: '没有权限访问该页面',
    },
  },
  setup(props) {
    let status = +props.value;
    const messages: {
      [key: number]: string;
    } = {
      404: '找不到页面',
      500: '服务器出错',
      403: props.errMessage,
      // TODO 待完善其他错误码
    };
    if (isNaN(status)) {
      status = 404;
    }

    return {
      status,
      message: messages[status],
    };
  },
});
</script>

<style scoped lang="less">
.http-status-error {
  margin-top: 18vh;
  text-align: center;

  &._403 {
    padding-top: 245px;
    background-image: url('@/assets/svgs/error/403.svg');
    background-repeat: no-repeat;
    background-position: center top;
  }

  &._404 {
    padding-top: 245px;
    background-image: url('@/assets/svgs/error/404.svg');
    background-repeat: no-repeat;
    background-position: center top;
  }

  &._500 {
    padding-top: 245px;
    background-image: url('@/assets/svgs/error/500.svg');
    background-repeat: no-repeat;
    background-position: center top;
  }

  .desc {
    padding-top: 15px;
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
    background-color: #D9EBFF;
    border-top-left-radius: 111px;
    transform: rotate(-67deg);
  }
}
</style>
