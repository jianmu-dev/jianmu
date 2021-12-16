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
          <router-link :to="{ name: 'project-group' }">
            <jm-tooltip content="分组管理" placement="top">
              <button class="group"></button>
            </jm-tooltip>
          </router-link>
          <router-link :to="{ name: 'secret-key' }">
            <jm-tooltip content="密钥管理" placement="top">
              <button class="secret-key"></button>
            </jm-tooltip>
          </router-link>
        </div>
      </div>
      <!-- 全部项目 -->
      <all-project v-if="searchResultFlag" />
      <!-- 搜索结果 -->
      <search-project
        :searchName="searchName"
        :projectGroupId="projectGroupId"
        v-else
      />
    </div>
    <bottom-nav />
  </div>
</template>

<script lang="ts">
import { computed, defineComponent } from 'vue';
import BottomNav from '@/views/nav/bottom2.vue';
import AllProject from '@/views/index/all-project.vue';
import SearchProject from '@/views/index/search-project.vue';

export default defineComponent({
  components: { AllProject, SearchProject, BottomNav },
  props: {
    searchName: {
      type: String,
    },
    projectGroupId: {
      type: String,
    },
  },
  setup(props) {
    // 切换到搜索结果页
    return {
      searchResultFlag: computed<boolean>(() => props.searchName === undefined),
    };
  },
});
</script>

<style scoped lang="less">
.index {
  .main {
    min-height: calc(100vh - 115px);

    .menu-bar {
      padding: 40px 0;
      display: flex;
      justify-content: space-between;
      align-items: center;

      .left-area {
        white-space: nowrap;

        button {
          width: 186px;
          height: 64px;
          background-color: #ffffff;
          box-shadow: 0px 6px 16px 4px #e6eef6;
          border-radius: 4px;
          border: 0;
          background-position: center center;
          background-repeat: no-repeat;
          cursor: pointer;

          &:active {
            opacity: 0.8;
          }

          &.add {
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
          &.group {
            margin-left: 40px;
            background-image: url('@/assets/svgs/index/group-icon.svg');
          }

          &.secret-key {
            margin-left: 40px;
            background-image: url('@/assets/svgs/index/secret-key-btn.svg');
          }
        }
      }
    }
  }
}
</style>
