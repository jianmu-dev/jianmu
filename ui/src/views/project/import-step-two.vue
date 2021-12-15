<template>
  <div class="import-step-two">
    <div class="right-top-btn">
      <router-link :to="{ name: 'index' }">
        <jm-button class="jm-icon-button-cancel" size="small" :loading="loading"
          >取消</jm-button
        >
      </router-link>
      <jm-button
        type="primary"
        class="jm-icon-button-previous"
        size="small"
        :loading="loading"
        @click="previous"
        >上一步
      </jm-button>
      <jm-button
        type="primary"
        class="jm-icon-button-preserve"
        size="small"
        :loading="loading"
        @click="save"
        >保存
      </jm-button>
    </div>
    <div class="content">
      <jm-scrollbar>
        <jm-radio-group v-model="importForm.dslPath">
          <jm-tree :props="treeProps" :load="loadNode" lazy>
            <template #default="{ data: { id, label, leaf } }">
              <jm-radio :disabled="!leaf" :label="id">{{ label }}</jm-radio>
            </template>
          </jm-tree>
        </jm-radio-group>
      </jm-scrollbar>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, getCurrentInstance, inject, ref } from 'vue';
import { _import, listGit } from '@/api/project';
import { IImportForm } from '@/model/modules/project';
import { useRouter } from 'vue-router';

/**
 * 计算节点目录
 * @param node
 * @param suffix
 */
function calculateNodeDir(node: any, suffix?: string): string {
  const dir = `/${node.data.label}${suffix || ''}`;

  if (!node.parent || !node.parent.data) {
    return dir;
  }

  return calculateNodeDir(node.parent, dir);
}

export default defineComponent({
  props: {
    data: {
      type: Object,
      required: true,
    },
  },
  setup(props) {
    const { proxy } = getCurrentInstance() as any;
    const router = useRouter();

    const importForm = ref<IImportForm>({
      id: props.data.git.id,
      ...props.data.gitCloneForm,
      dslPath: '',
    });
    const loading = ref<boolean>(false);
    const previousStep = inject('previousStep') as () => void;
    const reloadMain = inject('reloadMain') as () => void;

    return {
      importForm,
      loading,
      treeProps: {
        isLeaf: 'leaf',
      },
      loadNode: async (node: any, resolve: any) => {
        const dir = node.level === 0 ? undefined : calculateNodeDir(node);
        const data = await listGit(importForm.value.id, dir);
        const nodes: object[] = [];

        Object.keys(data)
          .sort()
          .forEach(key => {
            nodes.push({
              id: `${dir || ''}/${key}`,
              label: key,
              leaf: !data[key],
            });
          });

        return resolve(nodes);
      },
      previous: () => {
        previousStep();
      },
      save: () => {
        if (!importForm.value.dslPath) {
          proxy.$info('请选择DSL文件');

          return;
        }

        loading.value = true;

        _import({ ...importForm.value })
          .then(() => {
            // 刷新流程定义列表
            reloadMain();

            proxy.$success('导入成功');

            router.push({ name: 'index' });
          })
          .catch((err: Error) => {
            // 关闭loading
            loading.value = false;

            proxy.$throw(err, proxy);
          });
      },
    };
  },
});
</script>

<style scoped lang="less">
.import-step-two {
  display: flex;
  justify-content: center;

  .right-top-btn {
    position: fixed;
    right: 20px;
    top: 78px;

    .jm-icon-button-cancel::before,
    .jm-icon-button-previous::before,
    .jm-icon-button-preserve::before {
      font-weight: bold;
    }

    a {
      margin-right: 10px;
    }
  }

  .content {
    min-width: 600px;
    margin: 16px 0;
    border: 1px solid #e6ebf2;
    height: calc(100vh - 284px);

    ::v-deep(.el-tree) {
      margin: 15px;

      .el-radio__input {
        transform: scale(0.8, 0.8);

        &.is-disabled {
          display: none;
        }
      }

      .el-radio__input.is-disabled + span.el-radio__label {
        //color: inherit;
        cursor: inherit;
        padding-left: 0;
      }

      .el-tree-node__expand-icon.is-leaf {
        //display: none;
        padding: 0;
        width: 5px;

        & + .el-radio {
          .el-radio__label {
            padding-left: 5px;
          }
        }
      }
    }
  }
}
</style>
