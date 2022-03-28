<template>
  <!-- 搜索结果 -->
  <div class="search-project">
    <div class="search">
      <jm-select v-model="selectValue" placeholder="请选择项目组" @change="selectOption">
        <jm-option
          v-for="item in groupOptions"
          :key="item.value"
          :value="item.value"
          :label="item.label"
        >
        </jm-option>
      </jm-select>
      <div class="search-container">
        <jm-input
          v-model="projectName"
          @change="searchProject"
          placeholder="请输入项目名称"
        />
        <i class="jm-icon-button-search"></i>
      </div>
    </div>
    <div class="project">
      <project-group
        v-if="initialized"
        :project-group="currentGroup"
        :name="currentSearchName"
        :pageable="true"
      />
    </div>
  </div>
</template>

<script lang="ts">
import { IProjectGroupVo } from '@/api/dto/project-group';
import { listProjectGroup } from '@/api/view-no-auth';
import ProjectGroup from '@/views/common/project-group.vue';
import { computed, defineComponent, inject, onBeforeMount, ref } from 'vue';
import { useRouter } from 'vue-router';

export default defineComponent({
  components: { ProjectGroup },
  props: {
    searchName: {
      type: String,
    },
    projectGroupId: {
      type: String,
    },
  },
  setup(props) {
    const router = useRouter();
    // 选择框内容
    const selectValue = ref<string>('');
    // 搜索关键字
    const projectName = ref<string | undefined>(props.searchName);
    // 当前组id
    const currentGroupId = ref<string | undefined>(props.projectGroupId);
    // 当前搜索关键字-保证第一次搜索正常，传递值赋给当前搜索关键字
    const currentSearchName = ref<string>(props.searchName as string);
    // 项目组
    const projectGroups = ref<IProjectGroupVo[]>([]);
    // 已初始化
    const initialized = ref<boolean>(false);
    // 当前组
    const currentGroup = computed<IProjectGroupVo | undefined>(() =>
      projectGroups.value.find(item => item.id === currentGroupId.value),
    );
    // 选择框内容
    const groupOptions = ref<{ value: string; label: string }[]>([]);
    onBeforeMount(async () => {
      projectGroups.value = await listProjectGroup();
      initialized.value = true;
      projectGroups.value.forEach(item => {
        groupOptions.value.push({ value: item.id, label: item.name });
      });
      groupOptions.value.forEach(item => {
        if (item.value === currentGroupId.value) {
          selectValue.value = item.value;
        }
      });
    });
    const reloadMain = inject('reloadMain') as () => void;
    // 下拉框-change请求
    const selectOption = async () => {
      currentSearchName.value = projectName.value as string;
      await router.push({
        name: 'index',
        query: {
          projectGroupId: selectValue.value,
          searchName: currentSearchName.value,
        },
      });
      await reloadMain();
      // 改变赋值id
      currentGroupId.value = selectValue.value;
    };
    return {
      projectName,
      selectValue,
      selectOption,
      groupOptions,
      currentGroup,
      currentSearchName,
      initialized,
      // 查询输入框
      searchProject: async () => {
        currentSearchName.value = projectName.value as string;
        await router.push({
          name: 'index',
          query: {
            projectGroupId: selectValue.value,
            searchName: currentSearchName.value,
          },
        });
        await reloadMain();
      },
    };
  },
});
</script>

<style scoped lang="less">
// 搜索结果
.search-project {
  background-color: #fff;
  min-height: calc(100vh - 300px);
  margin-bottom: 20px;

  .search {
    display: flex;
    align-items: center;
    box-sizing: border-box;
    margin: 0 0 18px;
    padding: 30px 20px 0;

    ::v-deep(.el-select) {
      width: 390px;
      height: 36px;
      border-radius: 2px;
      margin-right: 30px;
    }

    ::v-deep(.el-input) {
      height: 36px;

      .el-input__inner {
        text-indent: 32px;
      }

      .el-input__inner:focus {
        text-indent: 32px;
        border: 1px solid #096dd9;
      }
    }

    // 搜索框缩进
    ::v-deep(.el-select) {
      .select-trigger {
        .el-input {
          .el-input__inner {
            text-indent: 1px;
          }

          .el-input__inner:focus {
            text-indent: 1px;
          }
        }
      }
    }

    .search-container {
      width: 100%;
      position: relative;

      .jm-icon-button-search::before {
        z-index: 100;
        content: '\e80b';
        position: absolute;
        left: 20px;
        top: 11px;
        cursor: pointer;
        color: #7f8c9b;
      }
    }
  }

  .project {
    padding: 0 20px;

    .project-group {
      margin-top: 10px;
    }
  }
}
</style>
