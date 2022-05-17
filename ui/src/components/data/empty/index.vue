<template>
  <el-empty :description="description" :image="image">
  </el-empty>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { ElEmpty } from 'element-plus';
import img from './svgs/no-data.svg';

export default defineComponent({
  name: 'jm-empty',
  props: {
    description: {
      type: String,
      default: '暂无数据',
    },
    image: {
      type: String,
      default: img,
    },
  },
  components: { ElEmpty },
});
</script>
