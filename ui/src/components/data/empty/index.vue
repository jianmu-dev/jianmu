<template>
  <el-empty :image="img"/>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { ElEmpty } from 'element-plus';
import img from './svgs/no-data.svg';

export default defineComponent({
  name: 'jm-empty',
  components: { ElEmpty },
  setup() {
    return {
      img,
    };
  },
});
</script>