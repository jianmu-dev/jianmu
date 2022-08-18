<template>
  <div class="jm-workflow-editor-custom-webhook-panel">
    <jm-form
      :model="{eventInstances,selectedReference}"
      ref="formRef"
      @submit.prevent
      label-position="top"
    >
      <div class="trigger-title">触发事件</div>
      <jm-form-item prop="selectedReference" :rules="nodeData.getFormRules().selectedReference">
        <jm-radio-group v-model="selectedReference" ref="radioGroupRef">
          <Event
            v-for="(event,idx) in form.events"
            :key="event.ref"
            :selected-reference="selectedReference"
            :name="event.name"
            :reference="event.ref"
            :ui-event="uiEvent?uiEvent[event.ref]:undefined"
            :index="idx"
            :available-params="event.availableParams.filter(({ ref }) => !event.eventRuleset.find((({ paramRef }) => paramRef === ref)))"
            :rules="nodeData.getFormRules().eventInstances.fields[idx]?.fields"
            :form-model-name="'eventInstances'"
            v-model:eventInstance="eventInstances[idx]"
            @update:eventInstance="updateEventInstance"
            @check-event="checkEvent"
          />
        </jm-radio-group>
      </jm-form-item>
    </jm-form>
  </div>
</template>

<script lang="ts">
import { defineComponent, onMounted, PropType, ref } from 'vue';
import { CustomWebhook, ICustomWebhookEventInstance } from '../../model/data/node/custom-webhook';
import Event from './form/custom-webhook-event.vue';
import { getWebhookVersionList, getWebhookVersionParams } from '@/api/custom-webhook';
import { pushCustomEvents } from '../../model/workflow-node';
import yaml from 'yaml';

export default defineComponent({
  components: { Event },
  props: {
    nodeData: {
      type: Object as PropType<CustomWebhook>,
      required: true,
    },
  },
  emits: ['form-created'],
  setup(props, { emit }) {
    const formRef = ref<HTMLFormElement>();
    const form = ref<CustomWebhook>(props.nodeData);
    const eventInstances = ref<(ICustomWebhookEventInstance | undefined)[]>(form.value.events.map(({ ref }) =>
      form.value.eventInstances.find(instance => instance.ref === ref)));
    const selectedReference = ref<string>('');
    const radioGroupRef = ref<any>();
    const uiEvent = ref<any>();

    onMounted(() => emit('form-created', formRef.value));

    onMounted(async () => {
      // 屏蔽radio-group keydown事件
      radioGroupRef.value.handleKeydown = () => {
      };
      // 获取版本列表
      const versionList = await getWebhookVersionList(form.value.ownerRef, form.value.nodeRef);
      // 获取版本参数
      const versionParams = await getWebhookVersionParams(form.value.ownerRef, form.value.nodeRef, versionList[0].version);
      form.value.events.length = 0;
      pushCustomEvents(form.value as CustomWebhook, versionParams.events, versionList[0].version, versionParams.dslText);
      // 覆盖参数，避免影响eventInstances
      // form.value.events = versionParams.events;
      const { ui } = yaml.parse(form.value.dslText);
      uiEvent.value = ui?.event;
    });

    return {
      formRef,
      form,
      eventInstances,
      selectedReference,
      radioGroupRef,
      uiEvent,
      updateEventInstance: () => {
        // 清空eventInstances，将过滤后的eventInstances push到form中
        form.value.eventInstances.length = 0;
        eventInstances.value.filter(eventInstance => eventInstance).forEach(item => {
          const { ref, ruleset, rulesetOperator } = item!;
          form.value.eventInstances.push({ ref, ruleset, rulesetOperator });
        });
      },
      checkEvent: (val: string) => {
        selectedReference.value = val;
      },
    };
  },
});
</script>

<style scoped lang="less">
.jm-workflow-editor-custom-webhook-panel {
  padding: 0 20px;

  .trigger-title {
    font-size: 14px;
    color: #082340;
    margin: 20px 0;
  }

  ::v-deep(.el-radio-group) {
    display: block;
    margin-left: 0;
  }

  ::v-deep(.el-form-item) {
    margin-bottom: 0;
  }
}
</style>