<template>
  <div class="task-definition-editor">
    <div class="right-top-btn">
      <router-link to="/task-definition">
        <jm-button class="jm-icon-button-cancel" size="small">取消</jm-button>
      </router-link>
      <jm-button type="primary" class="jm-icon-button-preserve" size="small"
                 @click="save" :loading="loading">保存
      </jm-button>
    </div>
    <jm-form :model="editorForm" :rules="editorRule" ref="editorFormRef" @submit.prevent>
      <div class="basic-section">
        <jm-form-item label="名称" label-position="top" prop="name" class="medium">
          <jm-input v-model="editorForm.name" :disabled="upgrade" clearable placeholder="请输入名称"/>
        </jm-form-item>
        <jm-form-item label="Ref" label-position="top" prop="ref" class="medium">
          <jm-input v-model="editorForm.ref" :disabled="upgrade" clearable placeholder="请输入Ref"/>
        </jm-form-item>
        <jm-form-item label="版本号" label-position="top" prop="version" class="small">
          <jm-input v-model="editorForm.version" clearable placeholder="请输入版本号"/>
        </jm-form-item>
        <jm-form-item label="描述" label-position="top" prop="description" class="large">
          <jm-input type="textarea" v-model="editorForm.description" clearable placeholder="请输入描述"/>
        </jm-form-item>
        <jm-form-item v-if="editorForm.outputParameters.length > 0" label="执行结果文件" label-position="top"
                      prop="resultFile" class="large">
          <jm-input v-model="editorForm.resultFile" clearable placeholder="请输入执行结果文件"/>
        </jm-form-item>
        <jm-form-item label="Worker类型" label-position="top" prop="type" class="medium">
          <jm-select v-model="editorForm.type" :disabled="upgrade">
            <jm-option label="请选择" value="" disabled/>
            <jm-option v-for="item in workerTypes" :key="item" :label="item" :value="item"/>
          </jm-select>
        </jm-form-item>
      </div>
      <div class="system-param-section" v-if="editorForm.type === 'DOCKER'">
        <div class="title">容器参数</div>
        <div class="content">
          <div class="main-params">
            <jm-form-item label="name" label-position="top" prop="spec.name" class="medium">
              <jm-input v-model="editorForm.spec.name" clearable placeholder="请输入name"/>
            </jm-form-item>
            <jm-form-item label="hostName" label-position="top" prop="spec.hostName" class="medium">
              <jm-input v-model="editorForm.spec.hostName" clearable placeholder="请输入hostName"/>
            </jm-form-item>
            <jm-form-item label="domainName" label-position="top" prop="spec.domainName" class="medium">
              <jm-input v-model="editorForm.spec.domainName" clearable placeholder="请输入domainName"/>
            </jm-form-item>
            <jm-form-item label="image" label-position="top" prop="spec.image" class="medium">
              <jm-input v-model="editorForm.spec.image" clearable placeholder="请输入image"/>
            </jm-form-item>
          </div>
          <div class="other-params">
            <jm-tabs v-model="systemParamTabActiveName">
              <jm-tab-pane name="command" lazy>
                <template #label>
                  <div class="tab">Command</div>
                </template>
                <div class="tab-content">
                  <jm-form-item label="command" label-position="top" prop="spec.cmd" class="large">
                    <jm-tags-input v-model="editorForm.spec.cmd" placeholder="请输入cmd，按Enter确定"/>
                  </jm-form-item>
                  <jm-form-item label="entrypoint" label-position="top" prop="spec.entrypoint" class="large">
                    <jm-tags-input v-model="editorForm.spec.entrypoint" placeholder="请输入entrypoint，按Enter确定"/>
                  </jm-form-item>
                  <jm-form-item label="workingDir" label-position="top" prop="spec.workingDir" class="medium">
                    <jm-input v-model="editorForm.spec.workingDir" clearable placeholder="请输入workingDir"/>
                  </jm-form-item>
                  <jm-form-item label="user" label-position="top" prop="spec.user" class="medium">
                    <jm-input v-model="editorForm.spec.user" clearable placeholder="请输入user"/>
                  </jm-form-item>
                </div>
              </jm-tab-pane>
            </jm-tabs>
          </div>
        </div>
      </div>
      <div class="business-param-section">
        <div class="title">业务参数</div>
        <div class="content">
          <div class="subtitle">
            <div>输入参数</div>
            <div>
              <jm-button type="text" icon="jm-icon-link-add" @click="openParamEditor('input')">新增输入参数</jm-button>
            </div>
          </div>
          <div class="subContent">
            <jm-table
              :data="editorForm.inputParameters"
              border>
              <jm-table-column
                label="参数唯一标识"
                align="center"
                prop="ref">
              </jm-table-column>
              <jm-table-column
                label="参数名称"
                align="center"
                prop="name">
              </jm-table-column>
              <jm-table-column
                label="参数类型"
                align="center"
                prop="type">
              </jm-table-column>
              <jm-table-column
                label="参数值"
                align="center"
                prop="value">
              </jm-table-column>
              <jm-table-column
                label="参数描述"
                align="center"
                prop="description">
              </jm-table-column>
              <jm-table-column
                label="操作"
                align="center"
                width="60">
                <template #default="{row}">
                  <jm-button type="text" size="small" @click="deleteParam('input', row)">删除</jm-button>
                </template>
              </jm-table-column>
            </jm-table>
          </div>
          <div class="subtitle separator">
            <div>输出参数</div>
            <div>
              <jm-button type="text" icon="jm-icon-link-add" @click="openParamEditor('output')">新增输出参数</jm-button>
            </div>
          </div>
          <div class="subContent">
            <jm-table
              :data="editorForm.outputParameters"
              border>
              <jm-table-column
                label="参数唯一标识"
                align="center"
                prop="ref">
              </jm-table-column>
              <jm-table-column
                label="参数名称"
                align="center"
                prop="name">
              </jm-table-column>
              <jm-table-column
                label="参数类型"
                align="center"
                prop="type">
              </jm-table-column>
              <jm-table-column
                label="参数值"
                align="center"
                prop="value">
              </jm-table-column>
              <jm-table-column
                label="参数描述"
                align="center"
                prop="description">
              </jm-table-column>
              <jm-table-column
                label="操作"
                align="center"
                width="60">
                <template #default="{row}">
                  <jm-button type="text" size="small" @click="deleteParam('output', row)">删除</jm-button>
                </template>
              </jm-table-column>
            </jm-table>
          </div>
        </div>
      </div>
    </jm-form>

    <jm-drawer
      :title="`新增${paramEditorType === 'input' ? '输入' : '输出'}参数`"
      size="30%"
      v-model="paramEditorDrawer"
      direction="rtl"
      destroy-on-close>
      <param-editor :type="paramEditorType" :current-refs="paramEditorRefs" @on-save="handleParamSave"/>
    </jm-drawer>
  </div>
</template>

<script lang="ts">
import { defineComponent, getCurrentInstance, inject, onBeforeMount, ref } from 'vue';
import { useStore } from 'vuex';
import ParamEditor from './param-editor.vue';
import { IRootState } from '@/model';
import { ICreateForm } from '@/model/modules/task-definition';
import { useRouter } from 'vue-router';
import { ITaskParameterVo } from '@/api/dto/task-definition';
import { create, createVersion, fetchVersionDetail } from '@/api/task-definition';

type ParamEditorType = 'input' | 'output';

export default defineComponent({
  components: {
    ParamEditor,
  },
  props: {
    // 任务定义ref
    taskDefRef: String,
    // 任务定义版本
    taskDefVersion: String,
  },
  setup(props: any) {
    const { proxy } = getCurrentInstance() as any;
    const router = useRouter();
    const { workerTypes } = useStore().state as IRootState;
    const reloadMain = inject('reloadMain') as () => void;

    const upgrade = ref<boolean>(!!props.taskDefRef);
    const editorFormRef = ref<any>(null);
    const editorForm = ref<ICreateForm>({
      name: '',
      ref: '',
      type: '',
      version: '',
      inputParameters: [],
      outputParameters: [],
      spec: {
        image: '',
      },
    });
    const editorRule = ref<object>({
      name: [
        { required: true, message: '名称不能为空', trigger: 'blur' },
      ],
      ref: [
        { required: true, message: 'Ref不能为空', trigger: 'blur' },
      ],
      version: [
        { required: true, message: '版本号不能为空', trigger: 'blur' },
      ],
      resultFile: [
        { required: true, message: '结果文件不能为空', trigger: 'blur' },
      ],
      type: [
        { required: true, message: 'Worker类型不能为空', trigger: 'change' },
      ],
      spec: {
        image: [
          { required: true, message: 'image不能为空', trigger: 'blur' },
        ],
      },
    });
    const paramEditorDrawer = ref<boolean>(false);
    const paramEditorType = ref<string>('');
    const paramEditorRefs = ref<string[]>([]);
    const loading = ref<boolean>(false);

    if (upgrade.value) {
      onBeforeMount(async () => {
        editorForm.value = await fetchVersionDetail(props.taskDefRef, props.taskDefVersion);
      });
    }

    return {
      workerTypes,
      upgrade,
      editorFormRef,
      editorForm,
      editorRule,
      loading,
      paramEditorDrawer,
      paramEditorType,
      paramEditorRefs,
      systemParamTabActiveName: ref<string>('command'),
      deleteParam: (type: ParamEditorType, value: ITaskParameterVo) => {
        proxy.$confirm(`确定要删除${type === 'input' ? '输入' : '输出'}参数吗?`, '', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning',
        }).then(() => {
          const params = (type === 'input' ? editorForm.value.inputParameters : editorForm.value.outputParameters) as ITaskParameterVo[];
          const index = params.findIndex(item => item.ref === value.ref);

          params.splice(index, 1);
        }).catch(() => {
        });
      },
      openParamEditor: (type: ParamEditorType) => {
        const params = (type === 'input' ? editorForm.value.inputParameters : editorForm.value.outputParameters) as ITaskParameterVo[];

        paramEditorType.value = type;
        paramEditorRefs.value = params.map(({ ref }) => ref);
        paramEditorDrawer.value = true;
      },
      handleParamSave: (type: ParamEditorType, value: ITaskParameterVo) => {
        ((type === 'input' ? editorForm.value.inputParameters : editorForm.value.outputParameters) as ITaskParameterVo[]).push(value);
        paramEditorDrawer.value = false;
      },
      save: () => {
        // 开启loading
        loading.value = true;

        editorFormRef.value.validate((valid: boolean) => {
          if (!valid) {
            // 关闭loading
            loading.value = false;

            return false;
          }

          proxy.create({ ...editorForm.value }).then(() => {
            // 刷新任务定义列表
            reloadMain();

            proxy.$success(upgrade.value ? '升级成功' : '新增成功');

            router.push({ name: 'task-definition' });
          }).catch((err: Error) => {
            // 关闭loading
            loading.value = false;

            proxy.$throw(err, proxy);
          });
        });
      },
      create: upgrade.value ? createVersion : create,
    };
  },
});
</script>

<style scoped lang="less">
.task-definition-editor {
  font-size: 14px;
  color: #333333;
  margin-bottom: 25px;

  .right-top-btn {
    position: fixed;
    right: 20px;
    top: 78px;

    .jm-icon-button-cancel::before,
    .jm-icon-button-preserve::before {
      font-weight: bold;
    }

    a {
      margin-right: 10px;
    }
  }

  .large, .medium, .small {
    margin-bottom: 8px;

    ::v-deep(.el-form-item__label) {
      float: none;
    }
  }

  .basic-section {
    padding: 10px 20% 16px 24px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    flex-wrap: wrap;
    background-color: #FFFFFF;
    border-radius: 4px;
    border: 1px solid #E6EBF2;

    .large {
      width: 100%;
    }

    .medium {
      width: 35%;
    }

    .small {
      width: 15%;
    }
  }

  .system-param-section {
    padding: 16px 24px;
    background-color: #F6F8FB;
    border-radius: 4px;

    .title {
      margin-bottom: 16px;
      font-weight: bold;
    }

    .content {
      padding: 0 15% 16px 24px;
      background-color: #FFFFFF;
      border: 1px solid #EEF0F7;

      .large {
        width: 100%;
      }

      .medium {
        width: 45%;
      }

      .main-params {
        display: flex;
        justify-content: space-between;
        align-items: center;
        flex-wrap: wrap;
      }

      .other-params {
        margin-top: 20px;

        .tab {
          width: 116px;
          height: 37px;
          text-align: center;
          background-color: #F6F8FB;
          border-radius: 6px 6px 0 0;
          border: 1px solid #EEF0F7;
        }

        .tab-content {
          padding: 0 24px 16px;
          border: 1px solid #EEF0F7;
          display: flex;
          justify-content: space-between;
          align-items: center;
          flex-wrap: wrap;
        }

        ::v-deep(.el-tabs) {
          .el-tabs__nav-wrap {
            box-shadow: none;
          }

          .el-tabs__nav-scroll {
            line-height: normal;
          }

          .el-tabs__item {
            padding-left: 0;
            padding-right: 0;
          }
        }
      }
    }
  }

  .business-param-section {
    margin-top: 16px;
    background-color: #FFFFFF;
    border-radius: 4px;

    .title {
      padding: 16px 24px;
      font-weight: bold;
      border-bottom: 1px solid #ECEEF6;
    }

    .content {
      padding: 0 24px 16px;

      .subtitle {
        font-weight: 400;
        display: flex;
        justify-content: space-between;
        align-items: center;

        &.separator {
          margin-top: 16px;
          border-top: 1px solid #ECEEF6;
        }
      }

      .subContent {
      }
    }
  }
}
</style>
