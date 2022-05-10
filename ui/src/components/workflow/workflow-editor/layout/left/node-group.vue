<template>
  <div class="group">
    <div class="fold-switch" @click="toggle">
      <i :class="['jm-icon-button-right',collapsed?'':'rotate']"></i>
      <span class="group-name">{{ groupName }}</span>
      <div class="loading" v-loading="loading"></div>
    </div>
    <div class="nodes" v-show="!collapsed && !loading">
      <!-- 网络异常 -->
      <div class="network-anomaly" v-if="networkAnomaly">
        <div class="anomaly-img"></div>
        <span class="anomaly-tip">网络开小差啦</span>
        <div class="reload-btn" @click="loadNodes(keyword,true)">重新加载</div>
      </div>
      <template v-else-if="nodes.length>0">
        <div class="nodes-wrapper">
          <x6-vue-shape
            v-for="item in nodes"
            :key="item.ref"
            :node-data="item"
            @mousedown="(e) => drag(item, e)"/>
        </div>
        <!-- 显示更多 -->
        <div class="load-more">
          <jm-load-more :state="loadState" :load-more="btnDown"></jm-load-more>
        </div>
      </template>
      <!-- 节点不存在 -->
      <jm-empty v-else></jm-empty>
    </div>
  </div>
</template>
<script lang="ts">
import { computed, defineComponent, getCurrentInstance, inject, onMounted, onUpdated, PropType, ref } from 'vue';
import X6VueShape from '../../shape/x6-vue-shape.vue';
import { IWorkflowNode } from '../../model/data/common';
import { NodeGroupEnum } from '../../model/data/enumeration';
import WorkflowNode from '../../model/workflow-node';
import { TimeoutError } from '@/utils/rest/error';
import { StateEnum } from '@/components/load-more/enumeration';
import WorkflowDnd from '../../model/workflow-dnd';

export default defineComponent({
  props: {
    type: {
      type: String as PropType<NodeGroupEnum>,
      required: true,
    },
    keyword: {
      type: String,
      required: true,
    },
  },
  components: {
    X6VueShape,
  },
  setup(props) {
    const groupName = computed<string>(() => {
      if (props.type === NodeGroupEnum.TRIGGER) {
        return '触发器';
      } else if (props.type === NodeGroupEnum.INNER) {
        return '内置节点';
      } else if (props.type === NodeGroupEnum.LOCAL) {
        return '本地节点';
      } else if (props.type === NodeGroupEnum.OFFICIAL) {
        return '官方节点';
      } else if (props.type === NodeGroupEnum.COMMUNITY) {
        return '社区节点';
      } else {
        return '';
      }
    });
    const { proxy } = getCurrentInstance() as any;
    // 分页的pageSize
    const pageSize: number = 6;
    // 显示更多组件状态
    const loadState = ref<StateEnum>(StateEnum.NONE);
    const loading = ref<boolean>(false);
    const collapsed = ref<boolean>(true);
    const nodes = ref<IWorkflowNode[]>([]);
    const keyWord = ref<string>(props.keyword);
    const getWorkflowDnd = inject('getWorkflowDnd') as () => WorkflowDnd;
    const workflowNode = new WorkflowNode();
    // 当前为第几页，默认第一页
    const currentPage = ref<number>(1);
    // 网络请求超时
    const networkAnomaly = ref<boolean>(false);
    /**
     * @param keyword 搜索关键字
     * @param reload 网络异常点击重试
     * @param currentPage 节点数据分页，当前第几页
     */
    const loadNodes = async (keyword: string, reload: boolean, currentPage: number = 1) => {
      // 点击重试，将请求超时的状态还原到初始状态
      if (reload) {
        networkAnomaly.value = false;
      }
      try {
        loading.value = true;
        switch (props.type) {
          // 触发器
          case NodeGroupEnum.TRIGGER:
            nodes.value = workflowNode.loadInnerTriggers(keyword);
            break;
          // 内置节点
          case NodeGroupEnum.INNER:
            nodes.value = workflowNode.loadInnerNodes(keyword);
            break;
          // 本地节点
          case NodeGroupEnum.LOCAL: {
            const { content, totalPages } = await workflowNode.loadLocalNodes(currentPage, pageSize, keyword);
            // 显示更多
            totalPages <= currentPage ? (loadState.value = StateEnum.NO_MORE) : (loadState.value = StateEnum.MORE);
            currentPage > 1 ? nodes.value = [...nodes.value, ...content] : nodes.value = content;
            break;
          }
          // 官方节点
          case NodeGroupEnum.OFFICIAL: {
            const {
              content,
              totalPages,
            } = await workflowNode.loadOfficialNodes(currentPage ? currentPage : 1, pageSize, keyword);
            // 显示更多
            totalPages <= currentPage ? (loadState.value = StateEnum.NO_MORE) : (loadState.value = StateEnum.MORE);
            currentPage > 1 ? nodes.value = [...nodes.value, ...content] : nodes.value = content;
            break;
          }
          // 社区节点
          default: {
            const {
              content,
              totalPages,
            } = await workflowNode.loadCommunityNodes(currentPage ? currentPage : 1, pageSize, keyword);
            // 显示更多
            totalPages <= currentPage ? (loadState.value = StateEnum.NO_MORE) : (loadState.value = StateEnum.MORE);
            currentPage > 1 ? nodes.value = [...nodes.value, ...content] : nodes.value = content;
            break;
          }
        }
        if (nodes.value.length <= 0) {
          return;
        }

        // 加载的节点数量，大于0分组展开
        collapsed.value = false;
        // 如果节点数量小于pageSize，隐藏显示更多组件
        if (nodes.value.length < pageSize) {
          loadState.value = StateEnum.NONE;
        }
      } catch (err) {
        // 网络超时
        if (err instanceof TimeoutError) {
          networkAnomaly.value = true;
        }
        proxy.$throw(err, proxy);
      } finally {
        loading.value = false;
        // 如果没有数据或者网络超时折叠
        if (nodes.value.length === 0 || networkAnomaly.value) {
          collapsed.value = true;
        }
      }
    };
    // 切换分组的折叠状态
    const toggle = async () => {
      collapsed.value = !collapsed.value;
    };
    // 显示更多
    const btnDown = async () => {
      loadState.value = StateEnum.LOADING;
      await loadNodes(keyWord.value, false, currentPage.value += 1);
    };
    onMounted(async () => {
      await loadNodes(keyWord.value);
    });
    onUpdated(async () => {
      if (keyWord.value === props.keyword) {
        return;
      }
      keyWord.value = props.keyword;
      // 搜索节点
      await loadNodes(keyWord.value);
      // 搜索后将currentPage初始化
      currentPage.value = 1;
    });
    return {
      groupName,
      loadState,
      networkAnomaly,
      loading,
      nodes,
      collapsed,
      toggle,
      btnDown,
      drag: (data: IWorkflowNode, event: MouseEvent) => {
        getWorkflowDnd().drag(data, event);
      },
      loadNodes,
    };
  },
});
</script>
<style lang="less" scoped>
@import '../../vars';

.group {
  display: flex;
  flex-direction: column;
  margin: 0 20px 10px;

  .fold-switch {
    cursor: pointer;
    display: flex;
    align-items: center;
    font-size: 14px;

    .jm-icon-button-right {
      width: 16px;
      height: 16px;
      line-height: 16px;
      transition: all .1s linear;

      &::before {
        font-size: 12px;
        color: #6B7B8D;
        transform: scale(0.8);
      }

      &.rotate {
        transform: rotate(90deg);
      }
    }

    .group-name {
      margin-left: 5px;
    }

    .loading {
      width: 14px;
      height: 14px;
    }

    font-weight: normal;
    color: @title-color;
  }

  .nodes {
    margin: 20px 0 0 5px;

    .network-anomaly {
      margin-top: 10px;
      width: 100%;
      display: flex;
      flex-direction: column;
      align-items: center;
      font-weight: 400;

      .anomaly-img {
        width: 56px;
        height: 56px;
        background-image: url("../../svgs/network.svg");
      }

      .anomaly-tip {
        margin: 20px 0 24px;
        font-size: 14px;
        color: #8095A9;
      }

      .reload-btn {
        cursor: pointer;
        font-size: 16px;
        color: #096DD9;
        margin-bottom: 10px;
      }
    }

    .nodes-wrapper {
      display: flex;
      flex-wrap: wrap;
    }

    .load-more {
      display: flex;
      justify-content: center;

      ::v-deep(.jm-load-more) {
        button {
          padding: 0;

          .icon {
            top: 15px;
            right: -15px;
            border-width: 5px;
          }
        }
      }
    }

    ::v-deep(.jm-workflow-x6-vue-shape) {
      margin: 0 20px 10px 10px;
      width: 64px;

      .x6-vue-shape-icon {
        width: 64px;
        height: 64px;
      }
    }
  }
}
</style>
