<template>
  <div class="others-dialog">
    对话框：
    <jm-button type="text" @click="dialogVisible = true">点击打开 Dialog</jm-button>

    <jm-dialog
      title="提示"
      v-model="dialogVisible"
      width="30%"
      :before-close="handleClose">
      <span>这是一段信息</span>
      <template #footer>
        <span class="dialog-footer">
          <jm-button size="small" @click="dialogVisible = false" icon="jm-icon-button-cancel">取 消</jm-button>
          <jm-button size="small" type="primary" @click="dialogVisible = false"
                     icon="jm-icon-button-confirm">确 定</jm-button>
        </span>
      </template>
    </jm-dialog>
  </div>
</template>

<script lang="ts">
import { defineComponent, getCurrentInstance, ref } from 'vue';

export default defineComponent({
  setup() {
    const { proxy } = getCurrentInstance() as any;

    return {
      dialogVisible: ref<boolean>(false),

      handleClose(done: any) {
        proxy.$root.$confirm('确认关闭？', '提示', {
          type: 'info',
        }).then(() => {
          done();
        }).catch(() => {
        });
      },
    };
  },
});
</script>

<style scoped lang="less">
.others-dialog {

}
</style>