<template>
  <div class="dialog">
    <jm-dialog :model-value="show" :width="350" top="calc((100vh - 344px) / 2)">
      <login @logged="logged">
        <template #tip>
          未登录状态下，操作内容将会丢失
        </template>
      </login>
    </jm-dialog>
  </div>
</template>

<script lang='ts'>
import { defineComponent, ref } from 'vue';
import Login from '@/views/common/login.vue';

export default defineComponent({
  components: {
    Login,
  },
  setup() {
    const show = ref<boolean>(true);
    return {
      show,
      logged() {
        show.value = false;
      },
    };
  },
});
</script>

<style scoped lang='less'>
.dialog {
  ::v-deep(.el-dialog) {
    border-radius: 4px;

    .el-dialog__header {
      padding: 16px 16px 0;
      height: 30px;
      border: none;

      .el-dialog__headerbtn {
        top: 10px;
        right: 16px;
      }
    }

    .el-dialog__body {
      padding: 0 30px 30px;
    }
  }
}
</style>
