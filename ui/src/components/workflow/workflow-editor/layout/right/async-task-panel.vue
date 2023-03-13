<template>
  <div class="jm-workflow-editor-async-task-panel">
    <jm-form :model="form" label-position="top" ref="formRef" @submit.prevent>
      <div class="set-padding">
        <jm-form-item label="节点名称" prop="name" class="name-item" :rules="nodeData.getFormRules().name">
          <jm-input v-model="form.name" show-word-limit :maxlength="36" />
        </jm-form-item>
        <jm-form-item label="节点版本" prop="version" :rules="nodeData.getFormRules().version" class="node-item">
          <jm-select
            v-loading="versionLoading"
            :disabled="versionLoading"
            v-model="form.version"
            placeholder="请选择节点版本"
            @change="changeVersion"
          >
            <jm-option v-for="item in versionList.versions" :key="item" :label="item" :value="item" />
          </jm-select>
          <div v-if="form.versionDescription ? !versionLoading : false" class="version-description">
            {{ form.versionDescription }}
          </div>
        </jm-form-item>
      </div>
      <div class="separate"></div>
      <div v-if="form.version">
        <div class="tab-container">
          <div :class="{ 'input-tab': true, 'selected-tab': tabFlag }" @click="tabFlag = true">
            输入参数
            <div class="checked-underline" v-if="tabFlag"></div>
          </div>
          <div :class="{ 'output-tab': true, 'selected-tab': !tabFlag }" @click="tabFlag = false">
            输出参数
            <div class="checked-underline" v-if="!tabFlag"></div>
          </div>
        </div>
        <div class="inputs-container set-padding" v-if="tabFlag">
          <jm-form-item
            v-for="(item, index) in form.inputs"
            :key="item.ref"
            :prop="`inputs.${index}.value`"
            :rules="nodeData.getFormRules().inputs.fields[index].fields.value"
            class="node-name"
          >
            <template #label>
              {{ item.name }}
              <jm-tooltip placement="top" v-if="item.description" :append-to-body="false" :content="item.description">
                <i class="jm-icon-button-help"></i>
              </jm-tooltip>
            </template>
            <secret-key-selector
              v-if="item.type === ParamTypeEnum.SECRET"
              v-model="item.value"
              :placeholder="item.description ? item.description : '请选择' + item.name"
            />
            <expression-editor
              v-else
              v-model="item.value"
              :node-id="nodeId"
              :placeholder="item.description ? item.description : '请输入' + item.name"
            />
          </jm-form-item>
          <div class="cache-item">
            <div class="cache-label">
              缓存挂载
              <jm-tooltip placement="top" :append-to-body="false" content="在顶部缓存模块中添加缓存后，在此挂载">
                <i class="jm-icon-button-help"></i>
              </jm-tooltip>
            </div>
            <cache-selector
              v-for="(item, index) in form.caches"
              :key="item.key"
              :index="index"
              v-model:cache-info="cachesInfo"
              v-model:name="item.name"
              v-model:value="item.value"
              :rules="form.getFormRules().caches.fields[index].fields"
              :form-model-name="'caches'"
              @update-disable="updateDisable"
              @update-cache="updateCache"
              @change-dir="changeDir"
              @delete-selected="deleteCacheSelector"
            />
            <div class="add-select-cache-btn">
              <span class="add-link" @click="addSelector">
                <i class="jm-icon-button-add" />
                <span>添加</span>
              </span>
            </div>
          </div>
          <jm-form-item
            label="执行失败时"
            class="node-item"
            prop="failureMode"
            :rules="nodeData.getFormRules().failureMode"
            v-if="failureVisible"
          >
            <jm-radio-group v-model="form.failureMode">
              <jm-radio :label="'suspend'">挂起</jm-radio>
              <jm-radio :label="'ignore'">忽略</jm-radio>
            </jm-radio-group>
          </jm-form-item>
        </div>
        <div class="outputs-container set-padding" v-else>
          <div v-if="form.outputs">
            <div v-for="item in form.outputs" :key="item.ref">
              <div class="label">
                <i class="required-icon" v-if="item.required"></i>
                {{ item.name }}
                <jm-tooltip placement="top" :append-to-body="false">
                  <template #content>
                    类型：{{ item.type }}<br />
                    <span v-if="item.value">描述：{{ item.description }}</span>
                  </template>
                  <i class="jm-icon-button-help"></i>
                </jm-tooltip>
              </div>
              <div class="content">
                <template v-if="item.value">
                  {{ item.value }}
                </template>
                <template v-else-if="item.description">
                  {{ item.description }}
                </template>
                <template v-else>暂无描述</template>
              </div>
            </div>
          </div>
          <div v-if="!form.outputs[0]">
            <jm-empty description="无输出参数" :image="noParamImage"></jm-empty>
          </div>
        </div>
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
import { INodeDefVersionListVo } from '@/api/dto/node-definitions';
import SecretKeySelector from './form/secret-key-selector.vue';
import ExpressionEditor from './form/expression-editor.vue';
import CacheSelector from './form/cache-selector.vue';
// eslint-disable-next-line no-redeclare
import { Node } from '@antv/x6';
import noParamImage from '../../svgs/no-param.svg';
import { pushParams } from '../../model/workflow-node';
import { v4 as uuidv4 } from 'uuid';

export default defineComponent({
  components: { SecretKeySelector, ExpressionEditor, CacheSelector },
  props: {
    nodeData: {
      type: Object as PropType<AsyncTask>,
      required: true,
    },
    caches: {
      type: [Array, String],
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
    const tabFlag = ref<boolean>(true);
    const changeVersion = async () => {
      form.value.inputs.length = 0;
      form.value.outputs.length = 0;
      try {
        versionLoading.value = true;
        failureVisible.value = false;
        if (props.nodeData.ownerRef === NodeGroupEnum.LOCAL) {
          const list = await getLocalNodeParams(form.value.getRef(), form.value.ownerRef, form.value.version);
          const { inputParameters: inputs, outputParameters: outputs, description: versionDescription } = list;
          pushParams(form.value as AsyncTask, inputs, outputs, versionDescription);
        } else {
          // eslint-disable-next-line no-redeclare
          const list = await getOfficialNodeParams(form.value.getRef(), form.value.ownerRef, form.value.version);
          // eslint-disable-next-line no-redeclare
          const { inputParams: inputs, outputParams: outputs, description: versionDescription } = list;
          pushParams(form.value as AsyncTask, inputs, outputs, versionDescription);
        }
      } catch (err) {
        proxy.$throw(err, proxy);
      } finally {
        versionLoading.value = false;
        failureVisible.value = true;
      }
    };

    // 模拟缓存列表
    const caches = ref<any>(props.caches || []);
    // 构造需要的数据
    const cachesInfo = ref<{ name: string; disable: boolean }[]>([]);
    if (typeof caches.value === 'string') {
      cachesInfo.value.push({ name: caches.value, disable: false });
    } else {
      caches.value.forEach((item: any) => {
        cachesInfo.value.push({ name: item.ref ? item.ref : item, disable: false });
      });
    }

    onMounted(() => {
      // 将已有的缓存禁用
      form.value.caches.forEach(item => {
        cachesInfo.value.forEach(_item => {
          if (item.name === _item.name) {
            _item.disable = true;
          }
        });
      });
    });

    const initSelect = () => {
      // 通过已选择的索引和未选择的索引进行禁用管理
      const cacheNameList = cachesInfo.value.map(({ name }) => name);
      const selectNameList = form.value.caches.map(({ name }) => name);
      const selectedIndex: any = [];
      selectNameList.forEach(item => {
        if (cacheNameList.indexOf(item) === -1) {
          return;
        }
        selectedIndex.push(cacheNameList.indexOf(item));
      });
      const totalIndex = [];
      for (let i = 0; i < cachesInfo.value.length; i++) {
        totalIndex.push(i);
      }
      const notSelect: any = [];
      cachesInfo.value.forEach((item, index) => {
        if (selectNameList.indexOf(item.name) === -1) {
          notSelect.push(index);
        }
      });
      selectedIndex.forEach((item: any) => {
        cachesInfo.value[item].disable = true;
      });
      notSelect.forEach((item: any) => {
        cachesInfo.value[item].disable = false;
      });
    };

    onMounted(async () => {
      if (form.value.version) {
        failureVisible.value = true;
      }
      versionLoading.value = true;
      // 获取versionList
      try {
        if (props.nodeData.ownerRef === NodeGroupEnum.LOCAL) {
          versionList.value = await getLocalVersionList(form.value.getRef(), form.value.ownerRef);
        } else {
          versionList.value = await getOfficialVersionList(form.value.getRef(), form.value.ownerRef);
        }

        if (!form.value.version && versionList.value.versions.length > 0) {
          form.value.version = versionList.value.versions[0];
          await changeVersion();
        }
      } catch (err) {
        proxy.$throw(err, proxy);
      } finally {
        versionLoading.value = false;
        // 等待异步数据请求结束才代码form创建成功（解决第一次点击警告按钮打开drawer没有表单验证）
        emit('form-created', formRef.value);
      }
    });

    return {
      formRef,
      form,
      versionList,
      ParamTypeEnum,
      nodeId,
      versionLoading,
      failureVisible,
      // 获取节点信息
      changeVersion,
      tabFlag,
      noParamImage,
      cachesInfo,
      addSelector: () => form.value.caches.push({ key: uuidv4(), name: '', value: '' }),
      // 更新选择框状态
      updateDisable: (val: string, index: number) => {
        form.value.caches[index].name = val;
        initSelect();
      },
      updateCache: (_index: number, cacheVal: string, dirVal: string) => {
        form.value.caches.forEach((item, index) => {
          if (_index === index) {
            item.name = cacheVal;
            item.value = dirVal;
          }
        });
      },
      changeDir: () => {
        form.value.caches.forEach((item, idx) => {
          formRef.value?.validateField(`caches.${idx}.value`);
        });
      },
      deleteCacheSelector: (_name: string, index: number) => {
        // 删除数据
        form.value.caches.splice(index, 1);
        // 还原列表
        cachesInfo.value.forEach(item => {
          if (item.name === _name) {
            item.disable = false;
          }
        });
        // 删除后校验
        form.value.caches.forEach((item, idx) => {
          formRef.value?.validateField(`caches.${idx}.value`);
        });
      },
    };
  },
});
</script>

<style scoped lang="less">
.jm-workflow-editor-async-task-panel {
  .set-padding {
    padding: 0 20px;

    ::v-deep(.cache-selector) {
      margin-bottom: 20px;
    }

    .add-select-cache-btn {
      height: 24px;
      font-weight: 400;
      font-size: 14px;
      line-height: 24px;
      color: #096dd9;
      margin-bottom: 26px;

      .add-link {
        cursor: pointer;
      }
    }
  }

  .name-item {
    margin-top: 20px;
  }

  .node-item {
    padding-top: 10px;

    &:last-child {
      margin-bottom: 20px;
    }
  }

  .jm-icon-button-help::before {
    margin: 0;
  }

  .node-name {
    padding-top: 10px;
  }

  .version-description {
    font-size: 12px;
    color: #7b8c9c;
    line-height: 20px;
    margin-top: 10px;
  }

  .separate {
    height: 6px;
    background: #fafbfc;
    margin-top: 20px;
  }

  .tab-container {
    display: flex;
    font-size: 14px;
    color: #7b8c9c;
    height: 50px;
    border-bottom: 1px solid #e6ebf2;
    margin-bottom: 10px;
    padding-left: 20px;

    .input-tab,
    .output-tab {
      line-height: 50px;
      width: 56px;
      display: flex;
      flex-direction: column;
      align-items: center;
      cursor: pointer;

      .checked-underline {
        width: 37px;
        border: 1px solid #096dd9;
        position: relative;
        top: -1px;
      }
    }

    .input-tab {
      margin-right: 40px;
    }

    .selected-tab {
      color: #096dd9;
    }
  }

  .cache-item {
    .cache-label {
      line-height: 20px;
      margin-bottom: 16px;
      padding-top: 10px;
      color: #3f536e;
      font-size: 14px;
    }
  }

  .outputs-container {
    font-size: 14px;

    .required-icon {
      display: inline-block;
      width: 6px;
      height: 6px;
      background: url('../../svgs/required-icon.svg');
      position: relative;
      top: -5px;
    }

    .label {
      color: #3f536e;
      margin-bottom: 10px;
      padding-top: 10px;
    }

    .content {
      color: #082340;
      background: #f6f8fb;
      border-radius: 2px;
      padding: 8px 17px 8px 14px;
      margin-bottom: 10px;
    }

    .el-empty {
      padding-top: 50px;
    }
  }
}
</style>
