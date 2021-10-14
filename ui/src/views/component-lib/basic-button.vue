<template>
  <div class="basic-button">
    按钮：
    <jm-button type="primary" class="jm-icon-button-on" size="small">主按钮</jm-button>
    <jm-button class="jm-icon-button-off" size="small">默认按钮</jm-button>
    <jm-button type="primary" size="small" :loading="true">加载中</jm-button>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';

export default defineComponent({});
</script>

<style scoped lang="less">
.basic-button {

}
</style>
