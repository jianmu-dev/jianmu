<template>
  <div class="index">
    <div class="main">
      <div class="menu-bar">
        <div class="left-area">
          <router-link :to="{ name: 'create-pipeline' }">
            <div class="btn-item">
              <button class="graph"></button>
              <span class="text">图形项目</span>
            </div>
          </router-link>
          <router-link :to="{ name: 'create-project' }">
            <div class="btn-item">
              <button class="code"></button>
              <span class="text">代码项目</span>
            </div>
          </router-link>
          <!--          <router-link :to="{ name: 'import-project' }">-->
          <!--            <div class="btn-item">-->
          <!--              <button class="git"></button>-->
          <!--              <span class="text">导入项目</span>-->
          <!--            </div>-->
          <!--          </router-link>-->
        </div>
        <div class="right-area">
          <router-link :to="{ name: 'node-library' }">
            <div class="btn-item">
              <button class="node-library"></button>
              <span class="text">本地节点</span>
            </div>
          </router-link>
          <router-link :to="{ name: 'project-group' }">
            <div class="btn-item">
              <button class="group"></button>
              <span class="text">分组管理</span>
            </div>
          </router-link>
          <router-link :to="{ name: 'secret-key' }">
            <div class="btn-item secret-key">
              <button class="secret-key"></button>
              <span class="text">密钥管理</span>
            </div>
          </router-link>
        </div>
      </div>
      <!-- 全部项目 -->
      <all-project v-if="searchResultFlag" />
      <!-- 搜索结果 -->
      <search-project :searchName="searchName" :projectGroupId="projectGroupId" v-else />
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
    min-height: calc(100vh - 130px);

    .menu-bar {
      margin-top: 20px;
      background-color: #ffffff;
      padding: 40px 20px;
      display: flex;
      justify-content: space-between;
      align-items: center;

      a {
        margin-right: 60px;

        &:last-child {
          margin-right: 0;
        }
      }

      .btn-item {
        display: flex;
        flex-direction: column;
        align-items: center;

        &:hover {
          .text {
            color: #096dd9;
          }
        }

        button {
          width: 56px;
          height: 56px;
          border: none;
          background-color: #ffffff;
          cursor: pointer;
          background-position: center center;
          background-repeat: no-repeat;

          &.graph {
            background-image: url('@/assets/svgs/index/graph-project-btn.svg');
          }

          &.code {
            background-image: url('@/assets/svgs/index/code-project-btn.svg');
          }

          &.git {
            background-image: url('@/assets/svgs/index/git-btn.svg');
          }

          &.node-library {
            background-image: url('@/assets/svgs/index/node-library-btn.svg');
          }

          &.group {
            background-image: url('@/assets/svgs/index/group-btn.svg');
          }

          &.secret-key {
            background-image: url('@/assets/svgs/index/secret-key-btn.svg');
          }
        }

        .text {
          font-size: 14px;
          margin-top: 6px;
          font-weight: 400;
          color: #082340;
        }
      }

      .left-area,
      .right-area {
        display: flex;
      }
    }
  }
}
</style>
