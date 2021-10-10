<template>
  <div class="form-cascader">
    级联选择器：
    <jm-cascader
      v-model="value"
      :options="options"
      @change="handleChange"></jm-cascader>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref } from 'vue';

export default defineComponent({
  setup() {
    const value = ref<string[]>([]);

    return {
      value,
      options: [{
        value: 'zhinan',
        label: '指南',
        children: [{
          value: 'shejiyuanze',
          label: '设计原则',
          children: [{
            value: 'yizhi',
            label: '一致',
          }, {
            value: 'fankui',
            label: '反馈',
          }, {
            value: 'xiaolv',
            label: '效率',
          }, {
            value: 'kekong',
            label: '可控',
          }],
        }, {
          value: 'daohang',
          label: '导航',
          children: [{
            value: 'cexiangdaohang',
            label: '侧向导航',
          }, {
            value: 'dingbudaohang',
            label: '顶部导航',
          }],
        }],
      }],
      handleChange(value) {
        console.log(value);
      },
    };
  },
});
</script>

<style scoped lang="less">
.form-cascader {

}
</style>