<template>
  <div class="index">
    <div class="main">
      <div class="menu-bar">
        <div class="left-area">
          <router-link :to="{ name: 'create-project' }">
            <jm-tooltip content="新增项目" placement="top">
              <button class="add"></button>
            </jm-tooltip>
          </router-link>
          <router-link :to="{ name: 'import-project' }">
            <jm-tooltip content="导入项目" placement="top">
              <button class="git"></button>
            </jm-tooltip>
          </router-link>
        </div>
        <div class="right-area">
          <router-link :to="{ name: 'node-library' }">
            <jm-tooltip content="本地节点库" placement="top">
              <button class="node-library"></button>
            </jm-tooltip>
          </router-link>
          <router-link :to="{ name: 'secret-key' }">
            <jm-tooltip content="密钥管理" placement="top">
              <button class="secret-key"></button>
            </jm-tooltip>
          </router-link>
        </div>
      </div>
      <div class="separator">全部项目</div>
      <project-group/>
    </div>
    <bottom-nav/>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import BottomNav from '@/views/nav/bottom2.vue';
import ProjectGroup from '@/views/common/project-group.vue';

export default defineComponent({
  components: { ProjectGroup, BottomNav },
});
</script>

<style scoped lang="less">
.index {
  .main {
    min-height: calc(100vh - 125px);

    .menu-bar {
      padding: 45px 0;
      display: flex;
      justify-content: space-between;
      align-items: center;

      .left-area {
        white-space: nowrap;

        button {
          width: 186px;
          height: 64px;
          background-color: #ffffff;
          box-shadow: 0 6px 14px 0 #acc3ee;
          border-radius: 4px;
          border: 0;
          background-position: center center;
          background-repeat: no-repeat;
          cursor: pointer;

          &:active {
            opacity: 0.8;
          }

          &.add {
            margin-left: 14px;
            background-image: url('@/assets/svgs/index/add-btn.svg');
          }

          &.git {
            margin-left: 40px;
            background-image: url('@/assets/svgs/index/git-btn.svg');
          }
        }
      }

      .right-area {
        margin-right: 0.5%;

        button {
          width: 48px;
          height: 48px;
          background-color: transparent;
          border: 0;
          background-position: center center;
          background-repeat: no-repeat;
          cursor: pointer;

          &:active {
            opacity: 0.8;
          }

          &.hub {
            background-image: url('@/assets/svgs/index/hub-btn.svg');
          }

          &.node-library {
            margin-left: 40px;
            background-image: url('@/assets/svgs/index/node-library-btn.svg');
          }

          &.secret-key {
            margin-left: 40px;
            background-image: url('@/assets/svgs/index/secret-key-btn.svg');
          }
        }
      }
    }

    .separator {
      margin-left: 0.5%;
      font-size: 24px;
      font-weight: bold;
      color: #082340;
      opacity: 0.5;
    }
  }
}
</style>
