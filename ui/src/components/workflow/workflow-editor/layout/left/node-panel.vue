<template>
  <div :class="{ 'jm-workflow-editor-node-panel': true, collapsed }" ref="container">
    <div class="collapse-btn jm-icon-button-left" @click="collapse"/>
    <div class="search">
      <jm-input placeholder="搜索" v-model="keyword" @change="changeKeyword" :clearable="true">
        <template #prefix>
          <i class="jm-icon-button-search"></i>
        </template>
      </jm-input>
    </div>
    <jm-scrollbar>
      <div class="groups" v-show="nodeCount>0">
        <node-group
          class="trigger" :type="NodeGroupEnum.TRIGGER" :keyword="tempKeyword" @get-node-count="getNodeCount"/>
        <node-group
          class="inner" :type="NodeGroupEnum.INNER" :keyword="tempKeyword" @get-node-count="getNodeCount"/>
        <node-group
          :type="NodeGroupEnum.LOCAL" :keyword="tempKeyword" @get-node-count="getNodeCount"/>
        <node-group
          :type="NodeGroupEnum.OFFICIAL" :keyword="tempKeyword" @get-node-count="getNodeCount"/>
        <node-group
          :type="NodeGroupEnum.COMMUNITY" :keyword="tempKeyword" @get-node-count="getNodeCount"/>
      </div>
      <div class="empty" v-if="nodeCount<=0">
        <jm-empty description="没有搜到相关结果" :image="noDataImage">
        </jm-empty>
        <div class="submit-issue" @click="submitIssue">
          欢迎提交节点需求
        </div>
      </div>
    </jm-scrollbar>
  </div>
</template>

<script lang="ts">
import { defineComponent, inject, onMounted, provide, ref } from 'vue';
import { Graph } from '@antv/x6';
import { WorkflowDnd } from '../../model/workflow-dnd';
import { WorkflowValidator } from '../../model/workflow-validator';
import NodeGroup from './node-group.vue';
import { NodeGroupEnum } from '../../model/data/enumeration';
import noDataImage from '../../svgs/no-data.svg';

export default defineComponent({
  components: { NodeGroup },
  emits: ['node-selected'],
  setup(props, { emit }) {
    const collapsed = ref<boolean>(false);
    const keyword = ref<string>('');
    // 输入框触发change事件后传递给组件的keyword
    const tempKeyword = ref<string>('');
    const getGraph = inject('getGraph') as () => Graph;
    const getWorkflowValidator = inject('getWorkflowValidator') as () => WorkflowValidator;
    let workflowDnd: WorkflowDnd;
    provide('getWorkflowDnd', () => workflowDnd);
    const container = ref<HTMLElement>();
    // 控制节点拖拽面板是否显示
    const nodeCount = ref<number>(0);
    const getNodeCount = (count: number) => {
      if (!count) {
        return;
      }
      // 如果node-group中都找不到节点拖拽面板不展示
      nodeCount.value += count;
    };
    // 提交issie
    const submitIssue = () => {
      window.open('https://gitee.com/jianmu-runners/jianmu-runner-list/issues', '_blank');
    };
    // 确定容器宽度
    onMounted(() => {
      // 初始化dnd
      workflowDnd = new WorkflowDnd(
        getGraph(),
        getWorkflowValidator(),
        container.value! as HTMLElement,
        (nodeId: string) => emit('node-selected', nodeId));
    });
    return {
      submitIssue,
      noDataImage,
      nodeCount,
      getNodeCount,
      NodeGroupEnum,
      collapsed,
      keyword,
      tempKeyword,
      container,
      collapse: () => {
        collapsed.value = container.value!.clientWidth > 0;
      },
      changeKeyword(key: string) {
        // 解决用户在输入框中输入了关键字但未进行搜索且点击清空内容，出现未搜索到的情况
        if (tempKeyword.value === key) {
          return;
        }
        tempKeyword.value = key;
        nodeCount.value = 0;
      },
    };
  },
});
</script>

<style scoped lang="less">
@import '../../vars';

@node-panel-top: 20px;
@collapse-btn-width: 36px;
.jm-workflow-editor-node-panel {
  // 折叠动画
  transition: width 0.3s ease-in-out;
  width: @node-panel-width;
  height: calc(100% - @node-panel-top);
  border: 1px solid #E6EBF2;
  background: #FFFFFF;
  position: absolute;
  left: 0;
  top: @node-panel-top;
  z-index: 2;

  &.collapsed {
    width: 0;

    .collapse-btn {
      // 反转
      transform: scaleX(-1);
      border-radius: 50% 0 0 50%;
      right: calc(-@collapse-btn-width * 2 / 2);

      &::before {
        margin-left: 5.5px;
      }
    }

    .search {
      opacity: 0;
    }
  }

  .collapse-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    box-sizing: border-box;
    border: 1px solid #EBEEFB;
    z-index: 3;
    position: absolute;
    top: 78px;
    right: calc(-@collapse-btn-width / 2);

    width: @collapse-btn-width;
    height: 36px;
    line-height: 36px;
    text-align: center;
    color: #6B7B8D;
    font-size: 16px;
    background-color: #FFFFFF;
    border-radius: 50%;
    cursor: pointer;

    &::before {
      margin-left: 1.5px;
    }
  }

  .search {
    position: absolute;
    top: 0;
    width: 100%;
    transition: opacity 0.3s ease-in-out;
    padding: 30px 0 30px;
    display: flex;
    justify-content: center;
    z-index: 2;
    background-color: #FFFFFF;

    ::v-deep(.el-input) {
      width: calc(100% - 40px);
    }

    border-bottom: 1px solid #EBEEFB;

    .jm-icon-button-search {
      font-size: 16px;
      color: #7B8C9C;
    }
  }

  ::v-deep(.el-scrollbar) {
    height: calc(100% - 97px);
    margin-top: 97px;
  }

  .groups {
    width: @node-panel-width;
  }

  .empty {
    font-size: 14px;
    text-align: center;
    margin-top: 20px;

    .submit-issue {
      cursor: pointer;
      color: @primary-color;
    }
  }
}
</style>
