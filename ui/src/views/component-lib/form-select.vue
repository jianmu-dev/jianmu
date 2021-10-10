<template>
  <div class="form-select">
    下拉框：
    <jm-select v-model="value" placeholder="请选择" clearable>
      <jm-option
        v-for="item in options"
        :key="item.value"
        :label="item.label"
        :value="item.value">
      </jm-option>
    </jm-select>


    <jm-select v-model="value" placeholder="请选择" disabled>
      <jm-option
        v-for="item in options"
        :key="item.value"
        :label="item.label"
        :value="item.value">
      </jm-option>
    </jm-select>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref } from 'vue';

export default defineComponent({
  setup() {
    return {
      options: ref([{
        value: '选项1',
        label: '黄金糕',
      }, {
        value: '选项2',
        label: '双皮奶',
      }, {
        value: '选项3',
        label: '蚵仔煎',
      }, {
        value: '选项4',
        label: '龙须面',
      }, {
        value: '选项5',
        label: '北京烤鸭',
      }]),
      value: ref(''),
    };
  },
});
</script>

<style scoped lang="less">
.form-select {
  > :nth-child(n) {
    margin-right: 10px;
  }
}
</style>