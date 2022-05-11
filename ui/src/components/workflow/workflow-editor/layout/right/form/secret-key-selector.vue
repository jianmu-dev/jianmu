<template>
  <div class="secret-key-selector" v-loading="loading">
    <jm-cascader :props="cascaderProps" clearable placeholder="请选择密钥" v-model="selectorValue"
                 @change="getValue"/>
  </div>
</template>

<script lang="ts">
import { defineComponent, PropType, ref } from 'vue';
import { listNamespace, listSecretKey } from '@/api/view-no-auth';

export interface ISecretKey {
  namespace: string;
  key: string;
}

export default defineComponent({
  props: {
    modelValue: {
      type: Object as PropType<ISecretKey>,
    },
  },
  emits: ['update:model-value'],
  setup(props, { emit }) {
    const selectorValue = ref<string[]>(props.modelValue ? [props.modelValue.namespace, props.modelValue.key] : []);
    const loading = ref<boolean>(selectorValue.value.length > 0);
    return {
      selectorValue,
      loading,
      getValue: (val: string[] | undefined) => {
        emit('update:model-value', val ? { namespace: val[0], key: val[1] } : undefined);
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