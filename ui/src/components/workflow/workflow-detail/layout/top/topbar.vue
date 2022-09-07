<template>
  <div class="jm-workflow-detail-topbar">
    <div class="left">
      <button class="jm-icon-button-left" @click="$emit('back')"></button>
      <div class="auto-left">
        <div class="first-row">
          <div class="title">{{ project.workflowName }}</div>
          <div class="vertical-divider"></div>
          <div v-show="entry" class="jm-icon-workflow-branch branch">{{ project.branch || 'master' }}</div>
          <div v-show="!entry" class="jm-icon-workflow-group" style="color: #096DD9"></div>
          <div v-show="!entry" class="project-group-name" @click="$emit('jump', project?.projectGroupId)">{{ project.projectGroupName }}</div>
        </div>
        <div class="second-row" v-if="project.workflowDescription">{{ project.workflowDescription }}</div>
      </div>
    </div>
    <div class="right">
      <!-- <button class="jm-icon-workflow-edit edit-button" @click="jumpToEdit">编辑</button> -->
      <button class="jm-icon-button-on trigger-button" @click="trigger" @keypress.enter.prevent>触发</button>
      <div v-show="!entry" class="vertical-divider"></div>
      <router-link v-if="!session && !entry" :to="{ name: 'login' }">
        <div class="no-login"></div>
      </router-link>
      <jm-dropdown v-else-if="session && !entry" trigger="click">
        <span class="el-dropdown-link">
          <jm-tooltip :content="session.username" placement="left">
            <span class="username">{{
                session.username.charAt(0).toUpperCase()
              }}</span>
          </jm-tooltip>
          <i class="el-icon-arrow-down el-icon--right"></i>
        </span>
        <template #dropdown>
          <jm-dropdown-menu>
            <jm-dropdown-item @click="$emit('logout')">退出</jm-dropdown-item>
          </jm-dropdown-menu>
        </template>
      </jm-dropdown>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, getCurrentInstance, PropType } from 'vue';
import { IProjectDetailVo } from '@/api/dto/project';
import { ISessionVo } from '@/api/dto/session';
import { DeatilTopbar } from '../../model/detail-topbar';
import { TriggerTypeEnum } from '@/api/dto/enumeration';

export default defineComponent({
  props: {
    project: {
      type: Object as PropType<IProjectDetailVo>,
      required: true,
    },
    entry: {
      type: Boolean,
      default: false,
    },
    session: Object as PropType<ISessionVo>,
  },
  emits: [ 'back', 'trigger', 'logout', 'jump', 'jump-to-edit'],
  setup(props, { emit }) {
    const { proxy } = getCurrentInstance() as any;
    const detailTopbar = new DeatilTopbar(props.project.id, (error?: Error):void=> {
      emit('trigger', error? error : undefined);
    });

    return {
      trigger(){
        const isWarning = props.project?.triggerType === TriggerTypeEnum.WEBHOOK;
        let msg = '<div>确定要触发吗?</div>';
        if (isWarning) {
          msg +=
            '<div style="color: red; margin-top: 5px; font-size: 12px; line-height: normal;">注意：已配置webhook，手动触发可能会导致不可预知的结果，请慎重操作。</div>';
        }
        proxy.$confirm(msg, '触发执行', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: isWarning ? 'warning' : 'info',
          dangerouslyUseHTMLString: true,
        }).then(() => {
          detailTopbar.trigger();
        }).catch(() => {});
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
  background: #FFFFFF;
  z-index: 3;
  font-size: 14px;
  color: #042749;
  padding: 0 30px;
  border-bottom: 1px solid #E6EBF2;

  display: flex;
  justify-content: space-between;
  align-items: center;

  button[class^="jm-icon-"] {
    border-radius: 2px;
    border-width: 0;
    background-color: transparent;
    color: #6B7B8D;
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
      background-color: #EFF7FF;
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
          background-color:#CDD1E3;
        }

        .svg-group {
          width: 25px;
          height: 25px;
          background-image: url("@/assets/svgs/index/group-btn.svg");
          background-size: cover;
        }
        .project-group-name {
          margin-left: 7px;
          height: 20px;
          font-size: 16px;
          font-weight: 400;
          color: #096DD9;
          line-height: 20px;
          cursor: pointer;
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
        color: #6B7B8D;
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
      background-color: #FFFFFF;
      border: 0.5px solid #CAD6EE;
      border-radius: 2px;
      cursor: pointer;

      &::before {
        font-size: 16px;
        margin-right: 6px;
      }

      &:hover {
        color: #0E70D9;
        background-color: #FFFFFF;
      }
    }
    .trigger-button {
      width: 90px;
      height: 36px;
      background: #E6F2FF;
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
        background-color: #EFF7FF;
        color: @primary-color;
      }
    }
    .vertical-divider {
      width: 1px;
      height: 20px;
      margin-left: 20px;
      margin-right: 10px;
      background-color: #CDD1E3;
    }

    .no-login {
      width: 36px;
      height: 36px;
      background-image: url('@/assets/svgs/nav/top/default-avatar.svg');
      background-position: center center;
      background-repeat: no-repeat;
    }

    .el-dropdown-link {
      display: inline-flex;
      align-items: center;
      margin-left: 10px;
      color: #333333;
      cursor: pointer;

      .username {
        display: inline-block;
        width: 36px;
        height: 36px;
        line-height: 36px;
        text-align: center;
        overflow: hidden;
        background-color: #7b8c9c;
        border-radius: 18px;
        font-size: 26px;
        font-weight: bold;
        color: #ffffff;
      }

      .el-icon-arrow-down::before {
        color: #082340;
      }
    }
  }
}
</style>
