<template>
  <div class="navigation-dropdown">
    下拉菜单：
    <jm-dropdown>
      <span class="el-dropdown-link">
        admin<i class="el-icon-arrow-down el-icon--right"></i>
      </span>
      <template #dropdown>
        <jm-dropdown-menu>
          <jm-dropdown-item>修改密码</jm-dropdown-item>
          <jm-dropdown-item>退出</jm-dropdown-item>
        </jm-dropdown-menu>
      </template>
    </jm-dropdown>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';

export default defineComponent({});
</script>

<style scoped lang="less">
.navigation-dropdown {
  .el-dropdown-link {
    color: #333333;

    .el-icon-arrow-down::before {
      color: #082340;
    }
  }
}
</style>