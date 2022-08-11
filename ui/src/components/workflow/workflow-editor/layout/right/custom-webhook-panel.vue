<template>
  <div class="jm-workflow-editor-custom-webhook-panel">
    <jm-form
      :model="form"
      ref="formRef"
      @submit.prevent
      label-position="top"
    >
      <div class="trigger-title">触发事件</div>
      <jm-radio-group v-model="selectedReference">
        <Event
          v-for="(event,idx) in form.events"
          :key="event.ref"
          :selected-reference="selectedReference"
          :name="event.name"
          :reference="event.ref"
          :available-params="event.availableParams"
          v-model:eventInstance="eventInstances[idx]"
          @update:eventInstance="updateEventInstance"
          @check-event="checkEvent"
        />
      </jm-radio-group>
    </jm-form>
  </div>
</template>

<script lang="ts">
import { defineComponent, onMounted, PropType, ref } from 'vue';
import { CustomWebhook, ICustomWebhookEventInstance } from '../../model/data/node/custom-webhook';
import Event from './form/custom-webhook-event.vue';
import { getWebhookVersionList, getWebhookVersionParams } from '@/api/custom-webhook';

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

    onMounted(() => emit('form-created', formRef.value));

    onMounted(async () => {
      // 获取版本列表
      const versionList = await getWebhookVersionList(form.value.ownerRef, form.value.nodeRef);
      // 获取版本参数
      const versionParams = await getWebhookVersionParams(form.value.ownerRef, form.value.nodeRef, versionList[0].version);
      // 覆盖参数，避免影响eventInstances
      form.value.events = versionParams.events;
    });

    return {
      formRef,
      form,
      eventInstances,
      selectedReference,
      updateEventInstance: () => {
        form.value.eventInstances = eventInstances.value.filter(eventInstance => eventInstance);
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
}
</style>