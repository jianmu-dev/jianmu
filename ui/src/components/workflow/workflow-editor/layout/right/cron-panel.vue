<template>
  <div class="jm-workflow-editor-cron-panel">
    <jm-form
      :model="form"
      :rules="form.getFormRules()"
      ref="formRef"
      @submit.prevent
    >
      <jm-form-item label="schedule" prop="schedule" class="schedule-item">
        <jm-input v-model="form.schedule" placeholder="请输入cron表达式"></jm-input>
      </jm-form-item>
      <div class="param-expression">
        <div class="title">Cron表达式的7个部分从左到右代表的含义如下：</div>
        <p>秒 分 时 日 月 周 年（可选，留空）</p>
        <div class="expression-example">
          <div class="title">常见表达式例子：</div>
          <p>0 0 12 * * ? 每天中午12点触发</p>
          <p>0 15 10 * * ? 每天上午10:15触发</p>
          <p>0 0 1 ? * SAT 每周六凌晨1点触发</p>
          <p>0 15 10 ? * MON-FRI 周一至周五的上午10:15触发</p>
          <p>0 * 10 * * ? 每天上午10点到10:59期间，每分钟触发一次</p>
        </div>
      </div>
    </jm-form>
  </div>
</template>

<script lang="ts">
import { defineComponent, onMounted, PropType, ref } from 'vue';
import { Cron } from '../../model/data/node/cron';

export default defineComponent({
  props: {
    nodeData: {
      type: Object as PropType<Cron>,
      required: true,
    },
  },
  emits: ['form-created'],
  setup(props, { emit }) {
    const formRef = ref();
    const form = ref<Cron>(props.nodeData);

    onMounted(() => emit('form-created', formRef.value));

    return {
      formRef,
      form,
    };
  },
});
</script>

<style scoped lang="less">
.jm-workflow-editor-cron-panel {
  padding: 0 20px;

  .schedule-item {
    margin-top: 20px;
  }

  .param-expression {
    color: #7B8C9C;
    font-size: 12px;
    line-height: 20px;
    user-select: text;
    -moz-user-select: text;
    -webkit-user-select: text;
    -ms-user-select: text;

    .title {
      font-weight: 560;
    }

    .expression-example {
      margin-top: 15px;
    }
  }
}
</style>