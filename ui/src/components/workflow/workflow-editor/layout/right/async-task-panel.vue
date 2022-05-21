<template>
  <div class="jm-workflow-editor-async-task-panel">
    <jm-form
      :model="form"
      label-position="top"
      ref="formRef"
      @submit.prevent
    >
      <jm-form-item label="节点名称" prop="name" class="name-item" :rules="nodeData.getFormRules().name">
        <jm-input v-model="form.name" show-word-limit :maxlength="36"/>
      </jm-form-item>
      <jm-form-item
        label="节点版本" prop="version" :rules="nodeData.getFormRules().version" class="node-item">
        <div v-loading="versionLoading" class="version-container">
          <jm-select
            v-model="form.version"
            placeholder="请选择节点版本"
            @change="changeVersion"
          >
            <jm-option v-for="item in versionList.versions" :key="item" :label="item" :value="item"/>
          </jm-select>
          <div class="version-description">{{ form.versionDescription }}</div>
        </div>
      </jm-form-item>
      <div v-if="form.inputs">
        <jm-form-item
          v-for="(item,index) in form.inputs"
          :key="item.ref"
          :prop="`inputs.${index}.value`"
          :rules="nodeData.getFormRules().inputs.fields[index].fields.value"
          class="node-name"
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
        <jm-form-item label="执行失败时" class="node-item" prop="failureMode" :rules="nodeData.getFormRules().failureMode"
                      v-if="failureVisible">
          <jm-radio-group v-model="form.failureMode">
            <jm-radio :label="'suspend'">挂起</jm-radio>
            <jm-radio :label="'ignore'">忽略</jm-radio>
          </jm-radio-group>
        </jm-form-item>
      </div>
    </jm-form>
  </div>
</template>

<script lang="ts">
import { defineComponent, getCurrentInstance, inject, onMounted, PropType, ref } from 'vue';
import { AsyncTask } from '../../model/data/node/async-task';
import { NodeGroupEnum, ParamTypeEnum } from '../../model/data/enumeration';
import {
  getLocalNodeParams,
  getLocalVersionList,
  getOfficialNodeParams,
  getOfficialVersionList,
} from '@/api/node-library';
import { INodeDefVersionListVo, INodeParameterVo } from '@/api/dto/node-definitions';
import SecretKeySelector from './form/secret-key-selector.vue';
import ExpressionEditor from './form/expression-editor.vue';
import { Node } from '@antv/x6';

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
    const getNode = inject('getNode') as () => Node;
    nodeId.value = getNode().id;
    const versionLoading = ref<boolean>(false);
    const failureVisible = ref<boolean>(false);

    onMounted(async () => {
      if (form.value.version) {
        failureVisible.value = true;
      }
      emit('form-created', formRef.value);
      versionLoading.value = true;
      // 获取versionList
      try {
        if (props.nodeData.ownerRef === NodeGroupEnum.LOCAL) {
          versionList.value = await getLocalVersionList(form.value.getRef(), form.value.ownerRef);
        } else {
          versionList.value = await getOfficialVersionList(form.value.getRef(), form.value.ownerRef);
        }
      } catch (err) {
        proxy.$throw(err, proxy);
      } finally {
        versionLoading.value = false;
      }
    });

    /**
     * push输入/输出参数
     * @param inputs
     * @param outputs
     */
    const pushParams = (inputs: INodeParameterVo[], outputs: INodeParameterVo[], versionDescription: string) => {
      form.value.versionDescription = versionDescription;
      if (inputs) {
        inputs.forEach(item => {
          form.value.inputs.push({
            ref: item.ref,
            name: item.name,
            type: item.type as ParamTypeEnum,
            value: (item.value || '').toString(),
            required: item.required,
            description: item.description,
          });
        });
      }
      if (outputs) {
        outputs.forEach(item => {
          form.value.outputs.push({
            ref: item.ref,
            name: item.name,
            type: item.type as ParamTypeEnum,
            value: (item.value || '').toString(),
            required: item.required,
            description: item.description,
          });
        });
      }
    };
    return {
      formRef,
      form,
      versionList,
      ParamTypeEnum,
      nodeId,
      versionLoading,
      failureVisible,
      // 获取节点信息
      changeVersion: async () => {
        form.value.inputs.length = 0;
        form.value.outputs.length = 0;
        try {
          versionLoading.value = true;
          failureVisible.value = false;
          if (props.nodeData.ownerRef === NodeGroupEnum.LOCAL) {
            const list = await getLocalNodeParams(form.value.getRef(), form.value.ownerRef, form.value.version);
            const { inputParameters: inputs, outputParameters: outputs, description: versionDescription } = list;
            pushParams(inputs, outputs, versionDescription);
          } else {
            const list = await getOfficialNodeParams(form.value.getRef(), form.value.ownerRef, form.value.version);
            const { inputParams: inputs, outputParams: outputs, description: versionDescription } = list;
            pushParams(inputs, outputs, versionDescription);
          }
        } catch (err) {
          proxy.$throw(err, proxy);
        } finally {
          versionLoading.value = false;
          failureVisible.value = true;
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

  .node-item {
    padding-top: 10px;
  }

  .jm-icon-button-help::before {
    margin: 0;
  }

  .node-name {
    padding-top: 10px;
  }

  .version-description {
    font-size: 12px;
    color: #7B8C9C;
    line-height: 18px;
    margin-top: 10px;
  }
}
</style>