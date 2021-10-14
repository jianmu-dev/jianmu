<template>
  <el-breadcrumb separator-class="el-icon-arrow-right">
    <slot></slot>
  </el-breadcrumb>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { ElBreadcrumb } from 'element-plus';

export default defineComponent({
  name: 'jm-breadcrumb',
  components: { ElBreadcrumb },
});
</script>