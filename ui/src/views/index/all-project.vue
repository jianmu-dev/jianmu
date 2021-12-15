<template>
  <!-- 所有项目 -->
  <div class="all-project">
    <div class="search">
      <i class="jm-icon-button-search" @click="searchProject"></i>
      <jm-input
        placeholder="请输入项目名称"
        v-model="projectName"
        @change="searchProject"
      />
    </div>
    <div class="project">
      <template v-if="initialized">
        <project-group
          v-for="projectGroup in projectGroups"
          :key="projectGroup.id"
          :project-group="projectGroup"
          :pageable="false"
        />
      </template>
    </div>
  </div>
</template>

<script lang="ts">
import { IProjectGroupVo } from '@/api/dto/project-group';
import { listProjectGroup } from '@/api/view-no-auth';
import ProjectGroup from '@/views/common/project-group.vue';
import { defineComponent, onBeforeMount, ref } from 'vue';
import { useRouter } from 'vue-router';
export default defineComponent({
  components: { ProjectGroup },
  setup() {
    const router = useRouter();
    const projectGroups = ref<IProjectGroupVo[]>([]);
    // 已初始化
    const initialized = ref<boolean>(false);
    // 项目名称
    const projectName = ref<string>('');
    onBeforeMount(async () => {
      const projectGroupList = await listProjectGroup();
      initialized.value = true;
      projectGroupList.forEach(item => {
        // 通过isShow筛选
        if (item.isShow) {
          projectGroups.value.push(item);
        }
      });
    });
    // 回车搜索
    const searchProject = () => {
      router.push({ name: 'index', query: { searchName: projectName.value } });
    };
    return {
      projectGroups,
      projectName,
      searchProject,
      initialized,
    };
  },
});
</script>

<style scoped lang="less">
// 所有项目
.all-project {
  background: #fff;
  .search {
    height: 66px;
    background: #f6fafe;
    display: flex;
    align-items: center;
    box-sizing: border-box;
    padding: 15px 20px;
    position: relative;
    ::v-deep(.el-input) {
      border-radius: 4px;
      .el-input__inner {
        height: 36px;
        text-indent: 22px;
      }
    }
    .jm-icon-button-search::before {
      z-index: 100;
      content: '\e80b';
      position: absolute;
      left: 33px;
      top: 24px;
      color: #7f8c9b;
    }
  }
  .project {
    padding: 0 20px 20px;
  }
}
</style>
