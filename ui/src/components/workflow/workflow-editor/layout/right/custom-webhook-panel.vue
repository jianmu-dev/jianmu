<template>
  <div class="jm-workflow-editor-custom-webhook-panel">
    <jm-form :model="{ eventInstances, selectedReference }" ref="formRef" @submit.prevent label-position="top">
      <jm-form-item label="版本" class="version-select" prop="version" :rules="nodeData.getFormRules().version">
        <jm-select v-model="form.version" placeholder="请选择版本" @change="changeVersion">
          <jm-option v-for="item in versionList.versions" :key="item" :label="item" :value="item" />
        </jm-select>
      </jm-form-item>
      <div class="trigger-title">触发事件</div>
      <jm-form-item prop="selectedReference" :rules="nodeData.getFormRules().selectedReference">
        <jm-radio-group v-model="selectedReference" ref="radioGroupRef">
          <custom-webhook-event
            v-for="(event, idx) in form.events"
            :key="event.ref"
            :selected-reference="selectedReference"
            :name="event.name"
            :reference="event.ref"
            :ui-event="uiEvent ? uiEvent[event.ref] : undefined"
            :index="idx"
            :available-params="filterParam(event)"
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
import { defineComponent, getCurrentInstance, onMounted, PropType, ref } from 'vue';
import { CustomWebhook, ICustomWebhookEventInstance } from '../../model/data/node/custom-webhook';
import CustomWebhookEvent from './form/custom-webhook-event.vue';
import { getWebhookVersionList, getWebhookVersionParams } from '@/api/custom-webhook';
import { INodeDefVersionListVo } from '@/api/dto/custom-webhook';
import { pushCustomEvents } from '../../model/workflow-node';
import yaml from 'yaml';

export default defineComponent({
  components: { CustomWebhookEvent },
  props: {
    nodeData: {
      type: Object as PropType<CustomWebhook>,
      required: true,
    },
  },
  emits: ['form-created'],
  setup(props, { emit }) {
    const { proxy } = getCurrentInstance() as any;
    const formRef = ref<HTMLFormElement>();
    const form = ref<CustomWebhook>(props.nodeData);
    const eventInstances = ref<(ICustomWebhookEventInstance | undefined)[]>(
      form.value.events.map(({ ref }) => form.value.eventInstances.find(instance => instance.ref === ref)),
    );
    const selectedReference = ref<string>('');
    const radioGroupRef = ref<any>();
    const versionList = ref<INodeDefVersionListVo>({ versions: [] });
    const uiEvent = ref<any>();

    const updateEventInstance = () => {
      // 清空eventInstances，将过滤后的eventInstances push到form中
      form.value.eventInstances.length = 0;
      eventInstances.value
        .filter(eventInstance => eventInstance)
        .forEach(item => {
          const { ref, ruleset, rulesetOperator } = item!;
          form.value.eventInstances.push({ ref, ruleset, rulesetOperator });
        });
    };

    const getVersionParam = async () => {
      // 清空events
      form.value.events.length = 0;
      try {
        const versionParams = await getWebhookVersionParams(
          form.value.ownerRef,
          form.value.nodeRef,
          form.value.version,
        );
        pushCustomEvents(form.value as CustomWebhook, versionParams.events, form.value.version, versionParams.dslText);
      } catch (err) {
        proxy.$throw(err, proxy);
      }
    };

    const changeVersion = async () => {
      // 清空eventInstances
      eventInstances.value = [];
      selectedReference.value = '';
      // 更新form
      updateEventInstance();
      await getVersionParam();
    };
    onMounted(async () => {
      // 屏蔽radio-group keydown事件
      // eslint-disable-next-line @typescript-eslint/no-empty-function
      radioGroupRef.value.handleKeydown = () => {};

      versionList.value = await getWebhookVersionList(form.value.ownerRef, form.value.nodeRef);
      if (form.value.version) {
        if (!form.value.dslText) {
          // 旧项目已有version但没有dslText
          await getVersionParam();
        }
      } else {
        if (versionList.value.versions.length > 0) {
          form.value.version = versionList.value.versions[0];
          await getVersionParam();
        }
      }
      const { ui } = yaml.parse(form.value.dslText);
      uiEvent.value = ui?.event || {};
      emit('form-created', formRef.value);
    });

    return {
      formRef,
      form,
      eventInstances,
      selectedReference,
      radioGroupRef,
      uiEvent,
      versionList,
      changeVersion,
      updateEventInstance,
      checkEvent: (val: string) => {
        selectedReference.value = val;
      },
      filterParam: (event: any) => {
        event.eventRuleset = event.eventRuleset ? event.eventRuleset : [];
        return event.availableParams.filter(({ ref }) => !event.eventRuleset.find(({ paramRef }) => paramRef === ref));
      },
    };
  },
});
</script>

<style scoped lang="less">
.jm-workflow-editor-custom-webhook-panel {
  padding: 0 20px;

  .version-select {
    margin-top: 20px;
  }

  .trigger-title {
    font-size: 14px;
    color: #3f536e;
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
