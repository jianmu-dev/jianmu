<template>
  <div class="notice-message">
    消息提示：
    <jm-button :plain="true" @click="open1()">成功</jm-button>
    <jm-button :plain="true" @click="open2()">警告</jm-button>
    <jm-button :plain="true" @click="open3()">消息</jm-button>
    <jm-button :plain="true" @click="open4()">错误</jm-button>
  </div>
</template>

<script lang="ts">
import { defineComponent, getCurrentInstance } from 'vue';

export default defineComponent({
  setup() {
    const { proxy } = getCurrentInstance() as any;
    
    return {
      open1() {
        proxy.$success({
          message: '恭喜你，这是一条成功消息',
          showClose: true,
        });
      },
      open2() {
        proxy.$warning({
          message: '警告哦，这是一条警告消息',
          showClose: true,
        });
      },
      open3() {
        proxy.$info({
          message: '这是一条消息提示',
          showClose: true,
        });
      },
      open4() {
        proxy.$error({
          message: '错了哦，这是一条错误消息',
          showClose: true,
        });
      },
    };
  },
});
</script>

<style scoped lang="less">
.notice-message {

}
</style>