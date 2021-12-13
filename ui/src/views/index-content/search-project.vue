<template>
  <!-- 搜索结果 -->
  <div class="search-project">
    <div class="search">
      <jm-select v-model="selectValue" @change="selectOption">
        <jm-option
          v-for="item in groupOptions"
          :key="item.value"
          :value="item.value"
          :label="item.label"
        >
        </jm-option>
      </jm-select>
      <div class="search-container">
        <jm-input v-model="projectName" @change="searchProject" />
        <i class="jm-icon-button-search"></i>
      </div>
    </div>
    <project-group
      :project-group="currentGroup"
      :name="projectName || ''"
      :pageable="true"
      :event-flag="eventFlag"
      @init-event-flag="initEventFlag"
    />
  </div>
</template>

<script lang="ts">
import { IProjectGroupVo } from '@/api/dto/project-group';
import { listProjectGroup } from '@/api/view-no-auth';
import ProjectGroup from '@/views/common/project-group.vue';
import { computed, defineComponent, onBeforeMount, onUpdated, ref } from 'vue';
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
    // 项目名称
    const projectName = ref<string | undefined>(props.searchName);
    // 控制事件触发时机
    const eventFlag = ref<boolean>(false);

    // 当前组id
    const currentGroupId = ref<string | undefined>(props.projectGroupId);
    // 项目组
    const projectGroups = ref<IProjectGroupVo[]>([]);
    // 当前组
    const currentGroup = computed<IProjectGroupVo | undefined>(() =>
      projectGroups.value.find(item => item.id === currentGroupId.value)
    );
    // 选择框内容
    const groupOptions = ref<{ value: string; label: string }[]>([]);
    onBeforeMount(async () => {
      projectGroups.value = await listProjectGroup();
      projectGroups.value.forEach(item => {
        groupOptions.value.push({ value: item.id, label: item.name });
      });
      groupOptions.value.forEach(item => {
        if (item.value === currentGroupId.value) {
          selectValue.value = item.label;
        }
      });
    });

    // 下拉框-change请求
    const selectOption = () => {
      router.push({
        name: 'index',
        query: {
          projectGroupId: selectValue.value,
          searchName: projectName.value,
        },
      });
      // 改变赋值id
      currentGroupId.value = selectValue.value;
      eventFlag.value = true;
    };
    const initEventFlag = () => {
      eventFlag.value = false;
    };
    // const reloadMain = inject('reloadMain') as () => void;
    return {
      projectName,
      selectValue,
      selectOption,
      groupOptions,
      currentGroup,
      eventFlag,
      initEventFlag,
      // 查询输入框
      searchProject: () => {
        router.push({
          name: 'index',
          query: {
            projectGroupId: selectValue.value,
            searchName: projectName.value,
          },
        });
        eventFlag.value = true;
        // reloadMain();
      },
    };
  },
});
</script>

<style scoped lang="less">
// 搜索结果
.search-project {
  .search {
    height: 66px;
    background: #f6fafe;
    display: flex;
    align-items: center;
    box-sizing: border-box;
    padding: 15px 30px;
    ::v-deep(.el-select) {
      width: 390px;
      height: 36px;
      border-radius: 2px;
      margin-right: 30px;
    }
    .search-container {
      width: 100%;
      position: relative;
      ::v-deep(.el-input) {
        height: 36px;
        border-radius: 2px;
        .el-input__inner {
          text-indent: 35px;
        }
      }
      .jm-icon-button-search::before {
        z-index: 100;
        content: '\e80b';
        position: absolute;
        left: 20px;
        top: 11px;
        cursor: pointer;
      }
    }
  }
}
</style>
