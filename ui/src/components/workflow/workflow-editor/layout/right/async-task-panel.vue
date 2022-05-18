<template>
  <div class="jm-workflow-editor-async-task-panel">
    <jm-form
      :model="form"
      label-position="top"
      ref="formRef"
      @submit.prevent
    >
      <jm-form-item label="节点名称" prop="name" class="name-item" :rules="nodeData.getFormRules().name">
        <jm-input v-model="form.name" clearable show-word-limit :maxlength="36"/>
      </jm-form-item>
      <jm-form-item label="节点版本" prop="version" :rules="nodeData.getFormRules().version">
        <jm-select
          v-model="form.version"
          placeholder="请选择节点版本"
          @change="changeVersion"
        >
          <jm-option v-for="item in versionList.versions" :key="item" :label="item" :value="item"/>
        </jm-select>
      </jm-form-item>
      <div v-if="form.inputs">
        <jm-form-item
          v-for="(item,index) in form.inputs"
          :key="item.ref"
          :prop="`inputs.${index}.value`"
          :rules="nodeData.getFormRules().inputs.fields[index].fields.value"
        >
          <template #label>
            {{ item.name }}
            <jm-tooltip
              placement="top"
              v-if="item.description"
              :append-to-body="false"
              :content="item.description"
            >
              <i class="jm-icon-button-help"></i>
            </jm-tooltip>
          </template>
          <secret-key-selector
            v-if="item.type === ParamTypeEnum.SECRET"
            v-model="item.value"
            :placeholder="item.description?item.description:'请选择'+item.name"
          />
          <expression-editor
            v-else
            v-model="item.value"
            :node-id="nodeId"
            :placeholder="item.description?item.description:'请输入'+item.name"/>
        </jm-form-item>
      </div>
    </jm-form>
  </div>
</template>

<script lang="ts">
import { defineComponent, getCurrentInstance, inject, onMounted, PropType, ref } from 'vue';
import { AsyncTask } from '../../model/data/node/async-task';
import { ParamTypeEnum } from '../../model/data/enumeration';
import {
  getLocalNodeParams,
  getLocalVersionList,
  getOfficialNodeParams,
  getOfficialVersionList,
} from '@/api/node-library';
import { INodeDefVersionListVo, INodeParameter, IOfficialParamsVo } from '@/api/dto/node-definitions';
import SecretKeySelector from './form/secret-key-selector.vue';
import ExpressionEditor from './form/expression-editor.vue';

export default defineComponent({
  components: { SecretKeySelector, ExpressionEditor },
  props: {
    nodeData: {
      type: Object as PropType<AsyncTask>,
      required: true,
    },
  },
  emits: ['form-created'],
  setup(props, { emit }) {
    const { proxy } = getCurrentInstance() as any;
    const formRef = ref();
    const form = ref<AsyncTask>(props.nodeData);
    // 版本列表
    const versionList = ref<INodeDefVersionListVo>({ versions: [] });
    const nodeId = ref<string>('');
    const node = inject('getNode');
    nodeId.value = node().id;

    onMounted(async () => {
      emit('form-created', formRef.value);
      // 获取versionList
      try {
        if (props.nodeData.ownerRef === 'local') {
          versionList.value = await getLocalVersionList(form.value.getRef(), form.value.ownerRef);
        } else {
          versionList.value = await getOfficialVersionList(form.value.getRef(), form.value.ownerRef);
        }
      } catch (err) {
        proxy.$throw(err, proxy);
      }
    });
    return {
      formRef,
      form,
      versionList,
      ParamTypeEnum,
      nodeId,
      // 获取节点信息
      changeVersion: async () => {
        form.value.inputs.length = 0;
        form.value.outputs.length = 0;

        try {
          if (props.nodeData.ownerRef === 'local') {
            const list = await getLocalNodeParams(form.value.getRef(), form.value.ownerRef, form.value.version);
            if (list.inputParameters) {
              list.inputParameters.forEach((param: INodeParameter) => form.value.inputs.push({
                ref: param.ref,
                name: param.name,
                type: param.type as ParamTypeEnum,
                value: (param.value || '').toString(),
                required: param.required,
                description: param.description,
              }));
            }
            if (list.outputParameters) {
              list.outputParameters.forEach((param: INodeParameter) => form.value.outputs.push({
                ref: param.ref,
                name: param.name,
                type: param.type as ParamTypeEnum,
                value: (param.value || '').toString(),
                required: param.required,
                description: param.description,
              }));
            }
          } else {
            const list = await getOfficialNodeParams(form.value.getRef(), form.value.ownerRef, form.value.version);
            if (list.inputParams) {
              list.inputParams.forEach((param: IOfficialParamsVo) => form.value.inputs.push({
                ref: param.ref,
                name: param.name,
                type: param.type as ParamTypeEnum,
                value: (param.value || '').toString(),
                required: param.required,
                description: param.description,
              }));
            }
            if (list.outputParams) {
              list.outputParams.forEach((param: IOfficialParamsVo) => form.value.outputs.push({
                ref: param.ref,
                name: param.name,
                type: param.type as ParamTypeEnum,
                value: (param.value || '').toString(),
                required: param.required,
                description: param.description,
              }));
            }
          }
        } catch (err) {
          proxy.$throw(err, proxy);
        }
      },
    };
  },
});
</script>

<style scoped lang="less">
.jm-workflow-editor-async-task-panel {
  margin-bottom: 25px;

  .name-item {
    margin-top: 20px;
  }

  .jm-icon-button-help::before {
    margin: 0;
  }
}
</style>