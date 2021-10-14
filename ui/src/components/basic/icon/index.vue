<template>
  <i v-if="name.startsWith('jm-icon-')" :class="name"/>
  <el-icon v-else :name="name"/>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { ElIcon } from 'element-plus';

export default defineComponent({
  name: 'jm-icon',
  components: { ElIcon },
  props: {
    name: {
      type: String,
      default: '',
    },
  },
});
</script>
