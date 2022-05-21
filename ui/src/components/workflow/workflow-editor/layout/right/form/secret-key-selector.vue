<template>
  <div class="secret-key-selector" v-loading="loading">
    <jm-cascader clearable :props="cascaderProps" :placeholder="placeholder" v-model="selectorValue"
                 @change="getValue"/>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref } from 'vue';
import { listNamespace, listSecretKey } from '@/api/view-no-auth';

export default defineComponent({
  props: {
    modelValue: {
      type: String,
      default: '',
    },
    placeholder: {
      type: String,
      required: true,
    },
  },
  emits: ['update:model-value'],
  setup(props, { emit }) {
    const selectorValue = ref<string[]>(props.modelValue ?
      props.modelValue.substring(2, props.modelValue.length - 2).split('.') : []);
    const loading = ref<boolean>(selectorValue.value.length > 0);
    return {
      selectorValue,
      loading,
      getValue: (val: string[] | undefined) => {
        emit('update:model-value', val ? `((${val[0]}.${val[1]}))` : undefined);
      },
      cascaderProps: {
        lazy: true,
        lazyLoad: async (node: any, resolve: any) => {
          const { level } = node;
          const nodes: object[] = [];

          if (level === 0) {
            const { list } = await listNamespace();
            list.forEach(({ name }) => nodes.push({
              value: name,
              label: name,
              leaf: false,
            }));
          } else {
            const { value } = node;
            const skArr = await listSecretKey(value);
            skArr.forEach(sk => nodes.push({
              value: sk,
              label: sk,
              leaf: true,
            }));
          }

          // 通过调用resolve将子节点数据返回，通知组件数据加载完成
          resolve(nodes);
          loading.value = false;
        },
      },
    };
  },
});
</script>