<template>
  <!-- 所有项目 -->
  <div class="search-project">
    <div class="search">
      <jm-select v-model="selectValue" @change="selectOption">
        <jm-option
          v-for="item in groupOptions"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        >
        </jm-option>
      </jm-select>
      <div class="search-container">
        <jm-input v-model="projectName" @change="searchProject" />
        <i class="jm-icon-button-search"></i>
      </div>
      <i class="jm-icon-button-refresh"></i>
    </div>
    <project-group :name="projectName" :pageable="true" />
  </div>
</template>

<script lang="ts">
import ProjectGroup from '@/views/common/project-group.vue';
import { defineComponent, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
// import { IQueryListForm } from '@/model/modules/project';

export default defineComponent({
  components: { ProjectGroup },
  props: {
    searchName: {
      type: String,
    },
  },
  setup() {
    const route = useRoute();
    const router = useRouter();
    // 选择框内容
    const selectValue = ref<string>('');
    // 项目名称
    const projectName = ref<string>('');
    // 请求内容
    // const requestContent = ref<IQueryListForm>({ pageNum: 1, pageSize: 20 });

    onMounted(() => {
      projectName.value = route.query.searchName as string;
      // requestContent.value.name = route.query.searchName as string;
      // console.log(requestContent.value);
      // console.log(projectName.value);
    });
    return {
      projectName,
      selectValue,
      // 下拉选择框
      selectOption: () => {
        // console.log(selectValue.value);
        router.push({
          name: 'index',
          query: {
            projectGroupId: selectValue.value,
            searchName: projectName.value,
          },
        });
      },
      // 查询输入框
      searchProject: () => {
        // console.log(projectName.value);
        router.push({
          name: 'index',
          query: {
            projectGroupId: selectValue.value,
            searchName: projectName.value,
          },
        });
      },
      groupOptions: ref([
        {
          value: 'Option1',
          label: 'Option1',
        },
        {
          value: 'Option2',
          label: 'Option2',
        },
        {
          value: 'Option3',
          label: 'Option3',
        },
        {
          value: 'Option4',
          label: 'Option4',
        },
        {
          value: 'Option5',
          label: 'Option5',
        },
      ]),
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
      margin-right: 20px;
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
    // 刷新
    .jm-icon-button-refresh::before {
      content: '\e80d';
      cursor: pointer;
    }
    // position: relative;
    // ::v-deep(.el-input) {
    //   border-radius: 4px;
    //   .el-input__inner {
    //     text-indent: 35px;
    //   }
    // }
  }
}
</style>
