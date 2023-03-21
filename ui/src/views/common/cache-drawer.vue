<template>
  <div class="cache-drawer-container">
    <jm-drawer
      :model-value="modelValue"
      @update:model-value="flag => $emit('update:model-value', flag)"
      :size="646"
      direction="rtl"
      destroy-on-close
    >
      <template #title>
        <div class="left">
          <div class="cache-t">
            <i class="jm-icon-workflow-cache icon"></i>
            <span>查看缓存</span>
          </div>
          <div class="cache-n">
            <jm-text-viewer :value="currentProjectName" />
          </div>
        </div>
      </template>
      <div class="content-wrapper" v-loading="loading">
        <jm-scrollbar height="calc(100vh - 65px)">
          <div class="cache-drawer">
            <cache-drawer-item v-for="(cache, index) in projectCache" :key="index" :cache-data="cache" />
          </div>
        </jm-scrollbar>
      </div>
    </jm-drawer>
  </div>
</template>

<script lang="ts">
import { defineComponent, getCurrentInstance, onMounted, ref } from 'vue';
import CacheDrawerItem from '@/views/common/cache-drawer-item.vue';
import { fetchProjectCache } from '@/api/view-no-auth';
import { IProjectCacheVo } from '@/api/dto/cache';

export default defineComponent({
  components: { CacheDrawerItem },
  props: {
    modelValue: {
      type: Boolean,
      required: true,
    },
    currentProjectWorkflowRef: {
      type: String,
      required: true,
    },
    currentProjectName: {
      type: String,
      required: true,
    },
  },
  emits: ['update:model-value'],
  setup(props) {
    const loading = ref<boolean>(false);
    const projectCache = ref<IProjectCacheVo>();
    const { proxy } = getCurrentInstance() as any;
    const getProjectCache = async () => {
      try {
        loading.value = true;
        projectCache.value = await fetchProjectCache(props.currentProjectWorkflowRef);
      } catch (err) {
        proxy.$throw(err, proxy);
      } finally {
        loading.value = false;
      }
    };
    onMounted(async () => {
      await getProjectCache();
    });
    return {
      loading,
      projectCache,
    };
  },
});
</script>

<style scoped lang="less">
.cache-drawer-container {
  position: absolute;

  .icon {
    width: 16px;
    height: 16px;
    font-size: 16px;
    display: flex;
    align-items: center;
    justify-content: center;
    overflow: hidden;

    &:before {
      margin: 0;
    }
  }

  ::v-deep(.el-drawer) {
    // 图标
    .el-drawer__header {
      display: flex;
      align-items: center;
      padding: 10px 24px;
      box-sizing: border-box;
      min-height: 65px;

      .left {
        flex: 1;
        color: #082340;

        .cache-t {
          line-height: 22px;
          display: flex;
          align-items: center;
          align-items: center;

          span {
            margin-left: 10px;
          }

          font-size: 16px;
          font-weight: 500;
        }

        .cache-n {
          line-height: 20px;
          font-size: 14px;
          font-weight: 400;
          margin-top: 4px;
          max-width: 530px;
        }
      }
    }

    .el-drawer__body {
      .content-wrapper {
        .el-scrollbar {
          background-color: #eff4f9;
        }
      }
    }
  }

  .cache-drawer {
    height: 100%;
    box-sizing: border-box;
    padding: 20px 24px 20px 24px;

    .cache-drawer-item {
      &:last-child {
        margin-bottom: 0;
      }
    }
  }
}
</style>
