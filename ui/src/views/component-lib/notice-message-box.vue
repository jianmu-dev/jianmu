<template>
  <div class="notice-message-box">
    弹框提示：
    <jm-button type="text" @click="open('success')">点击打开 success Message Box</jm-button>
    <jm-button type="text" @click="open('warning')">点击打开 warning Message Box</jm-button>
    <jm-button type="text" @click="open('info')">点击打开 info Message Box</jm-button>
    <jm-button type="text" @click="open('error')">点击打开 error Message Box</jm-button>
  </div>
</template>

<script lang="ts">
import { defineComponent, getCurrentInstance } from 'vue';

export default defineComponent({
  setup() {
    const { proxy } = getCurrentInstance() as any;

    return {
      open: function (type: string) {
        proxy.$confirm('此操作将永久删除该文件, 是否继续?', '标题消息提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type,
        }).then(() => {
          proxy.$success({
            message: '删除成功!',
          });
        }).catch(() => {
          proxy.$info({
            message: '已取消删除',
          });
        });
      },
    };
  },
});
</script>

<style scoped lang="less">
.notice-message-box {

}
</style>