<template>
  <el-pagination
    :page-sizes="[10, 20, 30, 40, 50]"
    :page-size="20"
    layout="total, prev, pager, next, sizes, jumper"/>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { ElPagination } from 'element-plus';

export default defineComponent({
  name: 'jm-pagination',
  components: { ElPagination },
});
</script>
