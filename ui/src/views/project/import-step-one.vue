<template>
  <div :class="{ 'import-step-one': true, authe }">
    <div class="right-top-btn">
      <router-link :to="{ name: 'index' }">
        <jm-button class="jm-icon-button-cancel" size="small" :loading="loading">{{ t('importStepOne.cancel')
          }}</jm-button>
      </router-link>
      <jm-button type="primary" class="jm-icon-button-next" size="small" :loading="loading" @click="next">{{
        t('importStepOne.next') }}
      </jm-button>
    </div>
    <jm-form :model="gitCloneForm" :rules="gitCloneFormRule" ref="gitCloneFormRef" @submit.prevent>
      <jm-form-item :label="t('importStepOne.subgroups')" prop="projectGroupId">
        <jm-select v-model="gitCloneForm.projectGroupId" :placeholder="t('importStepOne.selectGroup')">
          <jm-option v-for="item in projectGroupList" :key="item.id" :label="item.name" :value="item.id">
          </jm-option>
        </jm-select>
      </jm-form-item>
      <jm-form-item label="URL" prop="uri">
        <jm-input v-model="gitCloneForm.uri" clearable :placeholder="t('importStepOne.enterUrl')" />
      </jm-form-item>
      <jm-form-item :label="t('importStepOne.branch')" prop="branch">
        <jm-input v-model="gitCloneForm.branch" clearable :placeholder="t('importStepOne.enterBranch')" />
      </jm-form-item>
      <jm-form-item :label="t('importStepOne.auth')">
        <jm-switch v-model="authe" @change="handleAutheChange" />
      </jm-form-item>
      <div v-if="authe" class="authentication">
        <div>
          <jm-form-item :label="t('importStepOne.type')" prop="credential.type">
            <jm-select v-model="gitCloneForm.credential.type" clearable disabled
              :placeholder="t('importStepOne.selectType')" @change="handleCredentialTypeChange">
              <jm-option v-for="type in types" :key="type" :label="type" :value="type"></jm-option>
            </jm-select>
          </jm-form-item>
          <template v-if="gitCloneForm.credential.type === ProjectImporterTypeEnum.HTTPS">
            <jm-form-item :label="t('importStepOne.username')" prop="credential.userKey">
              <jm-cascader :props="cascaderProps" clearable :placeholder="t('importStepOne.selectUsername')"
                @change="handleUserKeyChange" />
            </jm-form-item>
            <jm-form-item :label="t('importStepOne.password')" prop="credential.passKey">
              <jm-select v-model="gitCloneForm.credential.passKey" clearable
                :placeholder="t('importStepOne.selectPassword')" :no-data-text="t('importStepOne.selectUsernameAgain')">
                <jm-option v-for="item in passKeys" :key="item" :label="item" :value="item"></jm-option>
              </jm-select>
            </jm-form-item>
          </template>
          <jm-form-item v-else-if="gitCloneForm.credential.type === ProjectImporterTypeEnum.SSH"
            :label="t('importStepOne.privateKey')" prop="credential.privateKey">
            <jm-cascader :props="cascaderProps" clearable :placeholder="t('importStepOne.selectPrivateKey')"
              @change="handlePrivateKeyChange" />
          </jm-form-item>
        </div>
      </div>
    </jm-form>
  </div>
</template>

<script lang="ts">
import {
  defineComponent,
  getCurrentInstance,
  inject,
  onMounted,
  ref,
} from 'vue';
import { IGitCloneForm } from '@/model/modules/project';
import { ProjectImporterTypeEnum } from '@/api/dto/enumeration';
import {
  listNamespace,
  listProjectGroup,
  listSecretKey,
} from '@/api/view-no-auth';
import { cloneGit } from '@/api/project';
import { IGitVo } from '@/api/dto/project';
import { IProjectGroupVo } from '@/api/dto/project-group';
import { useLocale } from '@/utils/i18n';

export default defineComponent({
  setup() {
    const { t } = useLocale();

    const { proxy } = getCurrentInstance() as any;

    const gitCloneFormRef = ref();
    const gitCloneForm = ref<IGitCloneForm>({
      uri: '',
      projectGroupId: '',
      credential: {
        type: ProjectImporterTypeEnum.HTTPS,
      },
      branch: 'master',
    });
    const projectGroupList = ref<IProjectGroupVo[]>([]);
    onMounted(async () => {
      // 请求项目组列表
      projectGroupList.value = await listProjectGroup();
    });
    const gitCloneFormRule = ref<object>({
      uri: [{ required: true, message: t('importStepOne.uriRequired'), trigger: 'blur' }],
      projectGroupId: [
        { required: true, message: t('importStepOne.groupRequired'), trigger: 'change' },
      ],
      branch: [{ required: true, message: t('importStepOne.branchRequired'), trigger: 'blur' }],
      credential: {
        type: [
          { required: true, message: t('importStepOne.mustSelectType'), trigger: 'change' },
        ],
        userKey: [
          { required: true, message: t('importStepOne.mustSelectUsername'), trigger: 'change' },
        ],
        passKey: [
          { required: true, message: t('importStepOne.mustSelectPassword'), trigger: 'change' },
        ],
      }
      ,
    });
    const passKeys = ref<string[]>([]);
    const loading = ref<boolean>(false);
    const authe = ref<boolean>(true);
    const nextStep = inject('nextStep') as (git: IGitVo, gitCloneForm: IGitCloneForm) => void;

    return {
      t,
      projectGroupList,
      gitCloneFormRef,
      gitCloneForm,
      gitCloneFormRule,
      passKeys,
      loading,
      authe,
      types: Object.values(ProjectImporterTypeEnum),
      handleAutheChange: () => {
        if (authe.value) {
          gitCloneForm.value.credential.type = ProjectImporterTypeEnum.HTTPS;

          return;
        }

        // 清空认证数据
        gitCloneForm.value.credential = {};
      },
      handleCredentialTypeChange: () => {
        delete gitCloneForm.value.credential.namespace;
        delete gitCloneForm.value.credential.userKey;
        delete gitCloneForm.value.credential.passKey;
        delete gitCloneForm.value.credential.privateKey;

        passKeys.value = [];
      },
      ProjectImporterTypeEnum,
      cascaderProps: {
        lazy: true,
        lazyLoad: async (node: any, resolve: any) => {
          const { level } = node;
          const nodes: object[] = [];

          if (level === 0) {
            const { list } = await listNamespace();
            list.forEach(({ name }) => nodes.push({
              value: name,
              label: name,
              leaf: false,
            }));
          } else {
            const { value } = node;
            const skArr = await listSecretKey(value);
            skArr.forEach(sk => nodes.push({
              value: sk,
              label: sk,
              leaf: true,
            }));
          }

          // 通过调用resolve将子节点数据返回，通知组件数据加载完成
          resolve(nodes);
        },
      },
      handleUserKeyChange: async (newVal: any) => {
        if (newVal) {
          gitCloneForm.value.credential.userKey = newVal[1];

          if (gitCloneForm.value.credential.namespace !== newVal[0]) {
            gitCloneForm.value.credential.namespace = newVal[0];

            delete gitCloneForm.value.credential.passKey;
          }

          passKeys.value = await listSecretKey(gitCloneForm.value.credential.namespace);
        } else {
          delete gitCloneForm.value.credential.namespace;
          delete gitCloneForm.value.credential.userKey;

          delete gitCloneForm.value.credential.passKey;

          passKeys.value = [];
        }
      },
      handlePrivateKeyChange: (newVal: any) => {
        if (newVal) {
          gitCloneForm.value.credential.namespace = newVal[0];
          gitCloneForm.value.credential.privateKey = newVal[1];
        } else {
          delete gitCloneForm.value.credential.namespace;
          delete gitCloneForm.value.credential.privateKey;
        }
      },
      next: () => {
        loading.value = true;

        gitCloneFormRef.value.validate((valid: boolean) => {
          if (!valid) {
            // 关闭loading
            loading.value = false;

            return false;
          }

          cloneGit({ ...gitCloneForm.value }).then((git: IGitVo) => {
            nextStep(git, { ...gitCloneForm.value });

            // 关闭loading
            loading.value = false;
          }).catch((err: Error) => {
            // 关闭loading
            loading.value = false;

            proxy.$throw(err, proxy);
          });
        });
      },
    };
  },
});
</script>

<style scoped lang="less">
.import-step-one {
  ::v-deep(.el-form-item) {
    &.is-required {
      display: flex;
      flex-direction: column;

      .el-form-item__label {
        text-align: left;
      }
    }

    .el-form-item__content {
      .el-select {
        width: 100%;
      }
    }
  }

  width: 500px;
  margin: 0 auto;
  padding: 16px 0;

  .right-top-btn {
    position: fixed;
    right: 20px;
    top: 78px;

    .jm-icon-button-cancel::before,
    .jm-icon-button-next::before {
      font-weight: bold;
    }

    a {
      margin-right: 10px;
    }
  }

  &.authe {
    width: 750px;

    .el-form-item {
      width: 65%;
    }
  }

  .authentication {
    background: rgba(238, 240, 243, 0.5);
    border-radius: 2px;
    padding: 20px;

    >div {
      padding: 0 24px;
      display: flex;
      justify-content: space-between;
      background-color: #FFFFFF;
      border-radius: 2px;
      border: 1px solid #B9CFE6;

      .el-form-item {
        width: auto;
      }
    }
  }

}
</style>