<template>
  <div class="jm-workflow-editor-custom-webhook-panel">
    <jm-form
      :model="form"
      ref="formRef"
      @submit.prevent
      label-position="top"
    >
      <div class="trigger-title">触发事件</div>
      <Event
        v-for="(event,idx) in form.events"
        :key="event.ref"
        :name="event.name"
        :reference="event.ref"
        :available-params="event.availableParams"
        v-model:eventInstance="eventInstances[idx]"
        @update:eventInstance="updateEventInstance"
      />
    </jm-form>
  </div>
</template>

<script lang="ts">
import { defineComponent, onMounted, PropType, ref } from 'vue';
import { CustomWebhook, ICustomWebhookEventInstance } from '../../model/data/node/custom-webhook';
import Event from './form/custom-webhook-event.vue';

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

    onMounted(() => emit('form-created', formRef.value));

    return {
      formRef,
      form,
      eventInstances,
      updateEventInstance: () => {
        form.value.eventInstances = eventInstances.value.filter(eventInstance => eventInstance);
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
}
</style>