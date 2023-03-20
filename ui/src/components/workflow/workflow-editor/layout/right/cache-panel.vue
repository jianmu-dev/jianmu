<template>
  <jm-drawer title="缓存" :size="410" direction="rtl" destroy-on-close @close="closeDrawer" @opened="dialogOpended">
    <template #title>
      <div class="title-content">
        <div class="title">缓存</div>
        <div class="description">添加缓存后可在节点中挂载，避免下次执行节点时重复下载依赖，提高执行速度。</div>
      </div>
    </template>
    <jm-scrollbar class="cache-scrollbar">
      <div class="panel-container">
        <div class="cache-label">
          <div class="label-title">唯一标识</div>
          <div class="label-description">以英文字母或下划线开头，支持下划线、数字、英文字母</div>
        </div>
        <jm-form @submit.prevent ref="cacheFormRef" :model="globalForm.caches" label-position="top">
          <cache-editor
            v-for="(item, index) in globalForm.caches"
            :key="item.key"
            v-model:reference="item.ref"
            :type="cacheTypes[item.key]"
            :index="index"
            :rules="globalForm.getCacheFormRules().caches.fields[index].fields"
            @change-cache="changeCache"
            @delete="handleDelete"
          />
        </jm-form>
        <div class="add-cache-btn">
          <span class="add-link" @click="addCache">
            <i class="jm-icon-button-add" />
            <span>添加缓存</span>
          </span>
        </div>
      </div>
    </jm-scrollbar>
    <jm-dialog v-model="delDialogVisible" width="428px">
      <template #title>
        <span class="dialog-title">确定删除当前缓存？</span>
      </template>
      删除后已引用该缓存的节点将会报错
      <template #footer>
        <jm-button @click="delDialogVisible = false">取消</jm-button>
        <jm-button type="primary" @click="delCache">确定</jm-button>
      </template>
    </jm-dialog>
  </jm-drawer>
</template>

<script lang="ts">
import { ref, defineComponent, onUpdated, getCurrentInstance, nextTick, PropType, inject, onMounted } from 'vue';
import CacheEditor from './form/cache-editor.vue';
import { v4 as uuidv4 } from 'uuid';
import { IWorkflow } from '../../model/data/common';
import { Global } from '../../model/data/global';

// 缓存类型，用于区分渲染
export enum CacheTypeEnum {
  // 编辑，回显已有的
  EDIT = 'EDIT',
  // 新增
  ADD = 'ADD',
}

export default defineComponent({
  components: { CacheEditor },
  props: {
    workflowData: {
      type: Object as PropType<IWorkflow>,
      required: true,
    },
  },
  emits: ['closed'],
  setup(props, { emit }) {
    const workflowForm = ref<IWorkflow>(props.workflowData);
    const globalForm = ref<Global>(new Global(props.workflowData.global));
    const cacheFormRef = ref<HTMLFormElement>();
    const delDialogVisible = ref<boolean>(false);
    const currentIndex = ref<number>();
    const cacheTypes: Record<string, CacheTypeEnum> = {};

    // 初始化构造遍历数据
    const cacheList: any = [];
    if (workflowForm.value.global.caches && workflowForm.value.global.caches?.length > 0) {
      if (typeof workflowForm.value.global.caches !== 'object') {
        const cache = { ref: workflowForm.value.global.caches, key: uuidv4() };
        cacheList.push(cache);
        cacheTypes[cache.key] = CacheTypeEnum.EDIT;
      } else {
        workflowForm.value.global.caches?.forEach(item => {
          const cache = { ref: item, key: uuidv4() };
          cacheList.push(cache);
          cacheTypes[cache.key] = CacheTypeEnum.EDIT;
        });
      }
      globalForm.value.caches = cacheList;
    }

    return {
      globalForm,
      cacheFormRef,
      delDialogVisible,
      cacheTypes,
      closeDrawer: async () => {
        workflowForm.value.global.caches = globalForm.value.caches;
        // eslint-disable-next-line @typescript-eslint/no-empty-function
        cacheFormRef.value!.validate().catch(() => {});
        emit('closed');
      },
      addCache: () => {
        const cache = { ref: '', key: uuidv4() };
        globalForm.value.caches.push(cache);
        cacheTypes[cache.key] = CacheTypeEnum.ADD;
      },
      changeCache: (val: string, oldVal: string, _index: number) => {
        globalForm.value.caches.find((item, index) => index === _index)!.ref = val;

        globalForm.value.caches.forEach(({ ref }, idx) => {
          if (_index === idx || oldVal !== ref) {
            return;
          }
          cacheFormRef.value?.validateField(`${idx}.ref`);
        });
      },
      // 点击删除
      handleDelete: (index: number) => {
        delDialogVisible.value = true;
        currentIndex.value = index;
      },
      // dialog
      delCache: () => {
        const cache = globalForm.value.caches.splice(currentIndex.value!, 1)[0];
        delete cacheTypes[cache.key];
        delDialogVisible.value = false;
      },
      dialogOpended: async () => {
        // 打开抽屉并且caches有值进行校验
        if (globalForm.value.caches.length === 0) {
          return;
        }
        // eslint-disable-next-line @typescript-eslint/no-empty-function
        cacheFormRef.value!.validate().catch(() => {});
      },
    };
  },
});
</script>

<style scoped lang="less">
.el-drawer {
  .el-drawer__header {
    .title-content {
      .description {
        margin-top: 8px;
        font-weight: 400;
        font-size: 14px;
        line-height: 22px;
        color: #7b8c9c;
      }
    }
  }

  .cache-scrollbar {
    height: calc(100vh - 180.5px);
  }

  .panel-container {
    padding: 20px 20px 0;

    .cache-label {
      box-sizing: border-box;
      padding: 8px;
      height: 56px;
      background: rgba(236, 242, 248, 0.4);
      border-radius: 4px;
      margin-bottom: 20px;

      .label-title {
        height: 20px;
        font-weight: 400;
        font-size: 14px;
        line-height: 20px;
        color: #3f536e;
      }

      .label-description {
        height: 18px;
        font-weight: 400;
        font-size: 12px;
        line-height: 18px;
        color: #7b8c9c;
        margin-top: 2px;
      }
    }

    ::v-deep(.cache-editor) {
      margin-bottom: 16px;
    }

    .add-cache-btn {
      height: 24px;
      font-weight: 400;
      font-size: 14px;
      line-height: 24px;
      color: #096dd9;

      .add-link {
        cursor: pointer;

        .jm-icon-button-add {
          display: inline-block;
          width: 24px;
          height: 24px;
        }
      }
    }
  }

  ::v-deep(.el-dialog) {
    .el-dialog__header {
      .dialog-title {
        font-size: 16px;
      }
    }
  }
}
</style>
