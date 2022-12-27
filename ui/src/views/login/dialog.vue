<template>
  <div class="dialog">
    <jm-dialog :model-value="show" :width="350" top="calc((100vh - 344px) / 2)">
      <login @logined="logined" type="dialog" @cancel="show = false">
        <template #tip> 未登录状态下，操作内容将会丢失 </template>
      </login>
    </jm-dialog>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, computed } from 'vue';
import Login from '@/views/common/login.vue';
import { useStore } from 'vuex';
import { useRouter } from 'vue-router';

export default defineComponent({
  components: {
    Login,
  },
  setup() {
    const store = useStore();
    const authMode = computed<string>(() => store.state.authMode);
    const router = useRouter();
    const show = ref<boolean>(true);
    switch (authMode.value) {
      // 开启验证模式，跳转到登录页
      case 'true':
        show.value = false;
        router.push({ name: 'login' });
        break;
      case 'false':
        show.value = false;
        break;
    }
    return {
      show,
      authMode,
      logined() {
        show.value = false;
      },
    };
  },
});
</script>

<style scoped lang="less">
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

      .login {
        min-height: 284px;
      }
    }
  }
}
</style>
