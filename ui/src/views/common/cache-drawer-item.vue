<template>
  <div class="cache-drawer-item">
    <div class="top">
      <div class="cache-name">
        <div class="identify" v-if="!originData.available">
          <span>不可用</span>
          <i class="jm-icon-button-warning icon"></i>
        </div>
        <jm-text-viewer :value="originData.name" />
      </div>
      <div class="clear-btn" @click="toClear"><i class="jm-icon-button-clear icon"></i>清理</div>
    </div>
    <div class="worker" v-if="originData.workerId">
      {{ originData.workerId }}
    </div>
    <div class="divider"></div>
    <div class="mount-point-wrapper">
      <div class="head" @click="toggle = !toggle" v-if="originData.nodeCaches.length > 0">
        <span>查看挂载点</span>
        <i :class="['jm-icon-button-collapse icon', toggle ? 'reverse' : '']"></i>
      </div>
      <div class="head none" v-else>
        <span>未设置挂载点</span>
      </div>
      <div class="content" v-if="toggle && originData.nodeCaches.length > 0">
        <div class="header">
          <div class="cell">节点</div>
          <div class="cell">挂载目录</div>
        </div>
        <div class="row" v-for="(node, i) in originData.nodeCaches" :key="i">
          <div class="cell">
            <div class="node-icon">
              <img :src="nodeICons[i]" alt="" />
            </div>
            <div class="node-name">
              <jm-text-viewer :value="node.name" />
            </div>
          </div>
          <div class="cell dir">
            <jm-text-viewer :value="node.path" />
          </div>
        </div>
      </div>
    </div>
    <jm-dialog v-model="dialogVisible" custom-class="center" title="确定清理当前缓存？" destroy-on-close width="428px">
      旧的缓存数据将被清空
      <template #footer>
        <span>
          <jm-button size="small" @click="dialogVisible = false">取消</jm-button>
          <jm-button size="small" type="primary" :loading="loading" @click="clear">确定</jm-button>
        </span>
      </template>
    </jm-dialog>
  </div>
</template>

<script lang="ts">
import { computed, defineComponent, getCurrentInstance, PropType, ref } from 'vue';
import { IProjectCacheVo } from '@/api/dto/cache';
import yaml from 'yaml';
import { clearCache } from '@/api/cache';
import defaultIcon from '@/components/workflow/workflow-editor/svgs/shape/async-task.svg';
import shellIcon from '@/components/workflow/workflow-editor/svgs/shape/shell.svg';

export default defineComponent({
  props: {
    cacheData: {
      required: true,
      type: Object as PropType<IProjectCacheVo>,
    },
  },
  setup(props) {
    const { proxy } = getCurrentInstance() as any;
    const toggle = ref<boolean>(false);
    const dialogVisible = ref<boolean>(false);
    const loading = ref<boolean>(false);
    const originData = computed<IProjectCacheVo>(() => props.cacheData);
    const nodeICons = ref<string[]>(
      originData.value.nodeCaches.map(item => {
        const data = yaml.parse(item.metadata);
        if (data.icon === undefined) {
          return shellIcon;
        }
        if (data.icon === null) {
          return defaultIcon;
        }
        return data.icon;
      }),
    );
    const toClear = () => {
      dialogVisible.value = true;
    };
    const clear = async () => {
      try {
        loading.value = true;
        await clearCache(originData.value.id);
        proxy.$success('清理成功');
        dialogVisible.value = false;
      } catch (err) {
        proxy.$throw(err, proxy);
      } finally {
        loading.value = false;
      }
    };
    return {
      loading,
      toggle,
      dialogVisible,
      toClear,
      originData,
      nodeICons,
      clear,
    };
  },
});
</script>

<style scoped lang="less">
.cache-drawer-item {
  padding: 16px;
  background: #ffffff;
  box-sizing: border-box;
  border: 1px solid #eceef6;
  font-size: 14px;
  line-height: 22px;
  font-weight: 400;
  color: #3f536e;
  margin-bottom: 20px;

  .top {
    display: flex;
    align-items: center;
    justify-content: space-between;

    .cache-name {
      width: 456px;
      color: #082340;
      font-weight: 500;
      display: flex;
      align-items: center;

      .identify {
        background: rgba(236, 77, 77, 0.1);
        border-radius: 12px;
        padding: 2px 6px 2px 8px;
        box-sizing: border-box;
        display: flex;
        align-items: center;
        color: #ec4d4d;
        width: 74px;
        height: 24px;

        .icon {
          margin-left: 2px;
        }

        span {
          flex-shrink: 0;
        }

        & + .jm-text-viewer {
          flex: 1;
          height: inherit;
          margin-left: 8px;
          max-width: 374px;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
        }
      }

      .jm-text-viewer {
        flex: 1;
        font-weight: 500;
        height: inherit;
      }
    }

    .clear-btn {
      cursor: pointer;
      display: flex;
      align-items: center;
      user-select: none;
      color: #096dd9;

      &:hover {
        color: #2e8de6;
      }

      .icon {
        margin-right: 2px;
      }
    }
  }

  .worker {
    display: inline-block;
    box-sizing: border-box;
    margin-top: 12px;
    padding: 4px 8px;
    background: #f0f3f5;
    border-radius: 1px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    max-width: 100%;
    color: #3f536e;
  }

  .divider {
    width: 100%;
    height: 1px;
    background-color: #eceef6;
    margin: 16px 0;
  }

  .mount-point-wrapper {
    .head {
      display: flex;
      cursor: pointer;
      user-select: none;
      align-items: center;
      color: #3f536e;

      &.none {
        cursor: text;
      }

      span {
        margin-right: 4px;
      }

      i {
        transform: rotate(0deg);

        &.reverse {
          transform: rotate(180deg);
        }
      }
    }

    .content {
      margin-top: 4px;
      background: #f5f7fa;

      .cell {
        width: 50%;
        padding: 8px 0 8px 16px;
        color: #082340;
        font-size: 14px;
        line-height: 22px;
        font-weight: 400;
        display: flex;
        align-items: center;
      }

      .header,
      .row {
        display: flex;
        align-items: center;
      }

      .header {
        .cell {
          font-weight: 500;
        }
      }

      .row {
        border-top: 1px solid #eceef6;

        .cell {
          .node-icon {
            width: 40px;
            height: 40px;
            border-radius: 25.5%;
            overflow: hidden;

            img {
              width: 100%;
              height: 100%;
            }
          }

          .node-name {
            flex: 1;
            margin-left: 10px;

            .jm-text-viewer {
              width: 100%;
            }
          }

          &.dir {
            flex: 1;
            padding-right: 16px;
            padding-left: 0;

            .jm-text-viewer {
              width: 100%;
            }
          }
        }
      }
    }
  }
}
</style>
