<template>
  <div class="jm-workflow-detail-topbar">
    <div class="left">
      <button class="jm-icon-button-left" @click="$emit('back')"></button>
      <div class="auto-left">
        <div class="first-row">
          <div class="title">{{ project.workflowName }}</div>
          <template v-if="project.branch">
            <div class="vertical-divider"></div>
            <div class="jm-icon-workflow-branch branch">{{ project.branch }}</div>
          </template>
        </div>
        <div class="second-row" v-if="project.workflowDescription">{{ project.workflowDescription }}</div>
      </div>
    </div>
    <div class="right">
      <!-- <button class="jm-icon-workflow-edit edit-button" @click="jumpToEdit">编辑</button> -->
      <button
        v-show="showStopAll"
        :class="[clicked ? 'clicked' : '']"
        class="jm-icon-button-stop stop-button"
        @click="terminateAll"
      >
        终止全部
      </button>
      <button class="jm-icon-button-on trigger-button" @click="trigger" @keypress.enter.prevent>触发</button>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, getCurrentInstance, PropType, ref } from 'vue';
import { IProjectDetailVo } from '@/api/dto/project';
import { ISession } from '@/model/modules/session';
import { DeatilTopbar } from '../../model/detail-topbar';
import { TriggerTypeEnum } from '@/api/dto/enumeration';

export default defineComponent({
  props: {
    project: {
      type: Object as PropType<IProjectDetailVo>,
      required: true,
    },
    session: Object as PropType<ISession>,
    showStopAll: {
      type: Boolean,
      default: false,
    },
  },
  emits: ['back', 'trigger', 'logout', 'jump', 'jump-to-edit'],
  setup(props, { emit }) {
    const { proxy } = getCurrentInstance() as any;
    const detailTopbar = new DeatilTopbar(
      props.project.id,
      props.project.workflowRef,
      (msg: string, error?: Error): void => {
        emit('trigger', msg, error ? error : undefined);
      },
    );

    const clicked = ref<boolean>(false);
    return {
      clicked,
      terminateAll() {
        const isWarning = props.project?.triggerType === TriggerTypeEnum.WEBHOOK;
        const msg = '<div>确定要终止执行中/挂起的全部实例吗？</div>';
        proxy
          .$confirm(msg, '终止全部', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: isWarning ? 'warning' : 'info',
            dangerouslyUseHTMLString: true,
          })
          .then((type: string) => {
            if (clicked.value) return;
            if (type === 'confirm') {
              clicked.value = true;
            }
            detailTopbar.terminateAllRecord();
          });
      },
      trigger() {
        const isWarning = props.project?.triggerType === TriggerTypeEnum.WEBHOOK;
        let msg = '<div>确定要触发吗?</div>';
        if (isWarning) {
          msg +=
            '<div style="color: red; margin-top: 5px; font-size: 12px; line-height: normal;">注意：已配置webhook，手动触发可能会导致不可预知的结果，请慎重操作。</div>';
        }
        proxy
          .$confirm(msg, '触发执行', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: isWarning ? 'warning' : 'info',
            dangerouslyUseHTMLString: true,
          })
          .then(() => {
            detailTopbar.trigger();
          });
      },
      jumpToEdit() {
        emit('jump-to-edit');
      },
    };
  },
});
</script>

<style lang="less">
@import '../../vars.less';

.jm-workflow-detail-topbar {
  height: @top-bar-height;
  background: #ffffff;
  z-index: 3;
  font-size: 14px;
  color: #042749;
  padding: 0 30px;
  border-bottom: 1px solid #e6ebf2;

  display: flex;
  justify-content: space-between;
  align-items: center;

  button[class^='jm-icon-'] {
    border-radius: 2px;
    border-width: 0;
    background-color: transparent;
    color: #6b7b8d;
    cursor: pointer;
    text-align: center;
    width: 24px;
    height: 24px;
    font-size: 18px;
    outline: none;

    &::before {
      font-weight: 500;
    }

    &:hover {
      background-color: #eff7ff;
      color: @primary-color;
    }
  }

  .left {
    display: flex;
    align-items: center;

    .auto-left {
      .first-row {
        display: flex;

        .title {
          margin-left: 20px;
          font-size: 16px;
        }

        .vertical-divider {
          margin: 0 20px;
          width: 1px;
          height: 20px;
          background-color: #cdd1e3;
        }

        .svg-group {
          width: 25px;
          height: 25px;
          background-image: url('@/assets/svgs/index/group-btn.svg');
          background-size: cover;
        }

        .jm-icon-workflow-branch:before {
          vertical-align: middle;
        }

        .branch {
          font-size: 16px;
          line-height: 20px;
          margin-left: -5px;
        }
      }

      .second-row {
        margin-left: 20px;
        font-size: 14px;
        color: #6b7b8d;
      }
    }
  }

  .right {
    display: flex;
    justify-content: space-around;
    align-items: center;

    .edit-button {
      width: 98px;
      height: 36px;
      font-size: 14px;
      padding-right: 5px;
      color: #082340;
      margin-right: 30px;
      background-color: #ffffff;
      border: 0.5px solid #cad6ee;
      border-radius: 2px;
      cursor: pointer;

      &::before {
        font-size: 16px;
        margin-right: 6px;
      }

      &:hover {
        color: #0e70d9;
        background-color: #ffffff;
      }
    }
    .stop-button {
      width: 98px;
      height: 36px;
      background: @default-background-color;
      margin-right: 24px;
      border-radius: 2px;
      text-indent: -2px;
      border: 0.5px solid #cad6ee;
      font-size: 14px;
      color: #116ed2;
      cursor: pointer;
      outline: none;

      &::before {
        font-size: 16px;
      }

      &:hover {
        background-color: #eff7ff;
        color: @primary-color;
      }
    }
    .clicked {
      cursor: no-drop;
    }

    .trigger-button {
      width: 90px;
      height: 36px;
      background: #e6f2ff;
      border-radius: 2px;
      padding-right: 5px;
      font-size: 14px;
      color: #116ed2;
      cursor: pointer;

      &::before {
        font-size: 16px;
        margin-right: 6px;
      }

      &:hover {
        background-color: #eff7ff;
        color: @primary-color;
      }
    }
  }
}
</style>
