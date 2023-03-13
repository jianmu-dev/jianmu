<template>
  <jm-drawer title="缓存" :size="410" direction="rtl" destroy-on-close v-model="visible" @close="closeDrawer">
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
            :key="item[index]"
            v-model:reference="item.ref"
            :type="item.type"
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
    <jm-dialog v-model="delDialogVisible" width="428px" title="确定删除当前缓存？">
      删除后节点中的缓存挂载会被一并删除
      <template #footer>
        <jm-button @click="delDialogVisible = false">取消</jm-button>
        <jm-button type="primary" @click="delCache">确定</jm-button>
      </template>
    </jm-dialog>
  </jm-drawer>
</template>

<script lang="ts">
import { ref, defineComponent, onUpdated, getCurrentInstance, nextTick, PropType, inject } from 'vue';
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
    modelValue: {
      type: Boolean,
      required: true,
    },
    workflowData: {
      type: Object as PropType<IWorkflow>,
      required: true,
    },
  },
  emits: ['closed'],
  setup(props, { emit }) {
    const visible = ref<boolean>(props.modelValue);
    const workflowForm = ref<IWorkflow>(props.workflowData);
    const globalForm = ref<Global>(new Global(props.workflowData.global));
    const cacheFormRef = ref<HTMLFormElement>();
    const delDialogVisible = ref<boolean>(false);
    const currentIndex = ref<number>();

    // 初始化构造遍历数据
    const cacheList: any = [];
    workflowForm.value.global.caches?.forEach(item => {
      cacheList.push({ ref: item, key: uuidv4(), type: CacheTypeEnum.EDIT });
    });
    globalForm.value.caches = cacheList;

    onUpdated(async () => {
      if (visible.value === props.modelValue) {
        return;
      }
      visible.value = props.modelValue;
      // 打开抽屉并且caches有值进行校验
      if (visible.value && globalForm.value.caches.length > 0) {
        await nextTick();
        // eslint-disable-next-line @typescript-eslint/no-empty-function
        cacheFormRef.value!.validate().catch(() => {});
      }
    });

    return {
      visible,
      globalForm,
      cacheFormRef,
      delDialogVisible,
      closeDrawer: () => {
        visible.value = false;
        workflowForm.value.global.caches = globalForm.value.caches;
        emit('closed', visible.value);
      },
      addCache: () => {
        globalForm.value.caches.push({ ref: '', key: uuidv4(), type: CacheTypeEnum.ADD });
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
        globalForm.value.caches.splice(currentIndex.value!, 1);
        delDialogVisible.value = false;
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
}
</style>
