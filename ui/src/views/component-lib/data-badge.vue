<template>
  <div class="data-badge">
    标记：
    <jm-badge :value="12" class="item">
      <jm-button size="small">评论</jm-button>
    </jm-badge>
    <jm-badge :value="3" class="item">
      <jm-button size="small">回复</jm-button>
    </jm-badge>
    <jm-badge :value="1" class="item" type="primary">
      <jm-button size="small">评论</jm-button>
    </jm-badge>
    <jm-badge :value="2" class="item" type="warning">
      <jm-button size="small">回复</jm-button>
    </jm-badge>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';

export default defineComponent({});
</script>

<style scoped lang="less">
.data-badge {
  .item {
    margin-top: 10px;
    margin-right: 40px;
  }
}
</style>