<template>
  <!-- 所有项目 -->
  <div class="all-project" v-loading="allProjectLoading">
    <div class="project-operator">
      <div class="project-list">
        <div class="text">项目列表</div>
        <jm-select @change="sortChange" :modelValue="sortType" popper-class="project-list-drop-down">
          <jm-option v-for="(item,index) in sortTypeList" :key="index" :label="item.label"
                     :value="item.value"></jm-option>
        </jm-select>
      </div>
      <div class="search">
        <i class="jm-icon-button-search" @click="searchProject"></i>
        <jm-input
          placeholder="请输入项目名称"
          v-model="projectName"
          @change="searchProject"
        />
      </div>
    </div>
    <div class="project">
      <template v-if="initialized && groupListRefresh">
        <project-group
          v-for="projectGroup in projectGroups"
          :key="projectGroup.id"
          :sortType="sortType"
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
import { computed, defineComponent, getCurrentInstance, inject, nextTick, onBeforeMount, onMounted, ref } from 'vue';
import { onBeforeRouteLeave, useRouter } from 'vue-router';
import { namespace } from '@/store/modules/project';
import { createNamespacedHelpers, useStore } from 'vuex';
import { SORT_TYPE_ENUM } from '@/api/dto/enumeration';

const { mapMutations } = createNamespacedHelpers(namespace);
export default defineComponent({
  components: { ProjectGroup },
  setup() {
    const { proxy } = getCurrentInstance() as any;
    const router = useRouter();
    const store = useStore();
    const projectGroups = ref<IProjectGroupVo[]>([]);
    // 已初始化
    const initialized = ref<boolean>(false);
    // 项目名称
    const projectName = ref<string>('');
    // 首页loading
    const allProjectLoading = ref<boolean>(false);

    // 改变项目组排序后强制数据及时刷新
    const groupListRefresh = ref<boolean>(true);
    // 项目组排序类型
    const sortTypeList = ref<Array<{ label: string, value: SORT_TYPE_ENUM }>>([
      { label: '默认排序', value: SORT_TYPE_ENUM.DEFAULT_SORT },
      { label: '最近修改', value: SORT_TYPE_ENUM.LAST_MODIFIED_TIME },
    ]);
    // 所有项目组在vuex中保存的排序类型
    const sortType = computed<SORT_TYPE_ENUM>(() => store.state[namespace].sortType);
    // 改变项目排序规则
    const sortChange = async (e: number) => {
      // 更改vuex中的项目组排序状态
      proxy.changeSortType(e);
      // 刷新项目组页面
      groupListRefresh.value = false;
      await nextTick();
      groupListRefresh.value = true;
    };
    onBeforeMount(async () => {
      try {
        allProjectLoading.value = true;
        const projectGroupList = await listProjectGroup();
        initialized.value = true;
        projectGroupList.forEach(item => {
          // 通过isShow筛选
          if (item.isShow) {
            projectGroups.value.push(item);
          }
        });
      } catch (err) {
        proxy.$throw(err, proxy);
      } finally {
        await nextTick(() => {
          allProjectLoading.value = false;
        });
      }
    });
    // 回车搜索
    const searchProject = () => {
      router.push({ name: 'index', query: { searchName: projectName.value } });
    };

    const setScrollbarOffset = inject('setScrollbarOffset') as () => void;
    const updateScrollbarOffset = inject('updateScrollbarOffset') as () => void;
    onMounted(() => setScrollbarOffset());
    onBeforeRouteLeave((to, from, next) => {
      updateScrollbarOffset();
      next();
    });

    return {
      projectGroups,
      projectName,
      searchProject,
      initialized,
      allProjectLoading,
      sortChange,
      ...mapMutations({ 'changeSortType': 'mutate' }),
      sortType,
      sortTypeList,
      groupListRefresh,
    };
  },
});
</script>

<style scoped lang="less">
// 所有项目
.all-project {
  background: #fff;
  margin-bottom: 20px;
  min-height: calc(100vh - 290px);

  .project-operator {
    overflow: hidden;
    margin: 0 0 30px;
    padding: 30px 20px 0;
    display: flex;
    justify-content: space-between;

    ::v-deep(.el-input) {
      border-radius: 4px;

      .el-input__inner {
        color: #6B7B8D;
        height: 36px;
        line-height: 36px;
      }

      .el-input__inner:focus {
        border: 1px solid #096dd9;
      }
    }

    .project-list {
      font-size: 20px;
      color: #6B7B8D;
      display: flex;
      align-items: center;

      .text {
        margin-right: 30px;
      }

      ::v-deep(.el-input) {
        width: 106px;
      }
    }

    .search {
      display: flex;
      align-items: center;
      box-sizing: border-box;
      position: relative;

      ::v-deep(.el-input) {
        width: 488px;

        .el-input__inner {
          text-indent: 1.5em;

          &::placeholder {
            text-indent: 1.5em;
          }
        }
      }

      .jm-icon-button-search::before {
        z-index: 100;
        content: '\e80b';
        position: absolute;
        left: 10px;
        top: 12px;
        color: #7f8c9b;
      }
    }
  }

  .project {
    padding: 0 20px 30px;
  }
}
</style>
