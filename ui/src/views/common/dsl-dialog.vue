<template>
  <jm-dialog
    :title="title"
    v-model="dialogVisible"
    width="800px"
    @close="close"
  >
    <div class="dsl" v-loading="loading">
      <jm-dsl-editor :value="dsl" readonly/>
    </div>
  </jm-dialog>
</template>

<script lang="ts">
import { computed, defineComponent, getCurrentInstance, onBeforeMount, ref, SetupContext } from 'vue';
import { DslTypeEnum } from '@/api/dto/enumeration';
import { fetchProjectDetail } from '@/api/view-no-auth';

export default defineComponent({
  props: {
    projectId: {
      type: String,
      required: true,
    },
    dslType: {
      type: String,
      required: true,
    },
  },
  // 覆盖dialog的close事件
  emits: ['close'],
  setup(props: any, { emit }: SetupContext) {
    const { proxy } = getCurrentInstance() as any;
    const dialogVisible = ref<boolean>(true);
    const loading = ref<boolean>(false);
    const dsl = ref<string>();
    const close = () => emit('close');

    const loadDsl = async () => {
      if (dsl.value) {
        return;
      }

      try {
        loading.value = true;

        const { dslText } = await fetchProjectDetail(props.projectId);
        dsl.value = dslText;
      } catch (err) {
        close();

        proxy.$throw(err, proxy);
      } finally {
        loading.value = false;
      }
    };

    onBeforeMount(() => loadDsl());

    return {
      title: computed(() => {
        let t = '';
        switch (props.dslType) {
          case DslTypeEnum.WORKFLOW:
            t = '流程';
            break;
          case DslTypeEnum.PIPELINE:
            t = '管道';
            break;
        }
        t += 'DSL';

        return t;
      }),
      dialogVisible,
      loading,
      dsl,
      close,
    };
  },
});
</script>

<style scoped lang="less">
.dsl {
  height: 60vh;
}
</style>