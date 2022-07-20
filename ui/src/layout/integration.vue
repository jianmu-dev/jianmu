<template>
  <div class="integration">
    <div class="top-tabs" v-if="isShowTop">
      <div class="tabs left">
        <router-link :to="{name:'index'}" @click="currentTab=0" class="tab-item">
          流水线
        </router-link>
        <router-link :to="{name:'ext-param'}" @click="currentTab=1" class="tab-item">
          外部参数
        </router-link>
        <router-link :to="{name:'secret-key'}" @click="currentTab=2" class="tab-item">
          密钥管理
        </router-link>
      </div>
      <div class="right" v-show="$route.name==='index'">
        <jm-input placeholder="搜索流水线" class="search-input" size="small" v-model="keyWord" @change="search" clearable>
          <template #prefix>
            <i class="jm-icon-button-search"></i>
          </template>
        </jm-input>
        <div class="operations">
          <div class="jm-icon-button-add code-assembly-line button" @click="selectBranch(0)">代码流水线</div>
          <div class="jm-icon-button-add graph-assembly-line button" @click="selectBranch(1)">图形流水线</div>
        </div>
      </div>
    </div>
    <div class="content">
      <router-view v-slot="{ Component }">
        <jm-scrollbar :max-height="'calc(100vh - 96px)'" ref="scrollBarRef">
          <component :is="Component" :keyword="key" v-model="flag" v-model:create-type="creatType"/>
        </jm-scrollbar>
      </router-view>
    </div>
  </div>
</template>

<script lang='ts'>
import { computed, defineComponent, getCurrentInstance, provide, ref } from 'vue';
import { onBeforeRouteUpdate, useRoute } from 'vue-router';

export default defineComponent({
  setup() {
    const { proxy } = getCurrentInstance() as any;
    const loadMain = ref<boolean>(true);
    const reloadMain = () => {
      loadMain.value = false;
      proxy.$nextTick(() => (loadMain.value = true));
    };
    const scrollBarRef = ref();
    provide('reloadMain', reloadMain);
    // 解决router-view中滚动位置被复用
    onBeforeRouteUpdate(async (to, from, next) => {
      // 将scrollbar滚动到顶部
      scrollBarRef.value.wrap.scrollTop = 0;
      next();
    });
    const currentTab = ref<number>(0);
    const route = useRoute();
    // 是否展示顶部导航
    const isShowTop = computed<boolean>(() => {
      return !((route.name === 'create-project') || (route.name === 'update-project'));
    });
    const keyWord = ref<string>('');
    const key = ref<string>('');
    const tabs = ref<Array<string>>(['流水线', '外部参数', '密钥管理']);
    // 控制选择分支弹框的开关
    const flag = ref<boolean>(false);
    // 判断创建什么流水线 ,0代表代码，1代表图形流水线
    const creatType = ref<number>(0);
    const selectBranch = (t: number) => {
      flag.value = true;
      creatType.value = t;
    };
    return {
      isShowTop,
      scrollBarRef,
      flag,
      creatType,
      tabs,
      keyWord,
      currentTab,
      key,
      search(k: string) {
        key.value = k;
      },
      selectBranch,
    };
  },
});
</script>

<style lang='less' scoped>
.integration {
  width: 1200px;
  margin: 0 auto;
  position: relative;

  //&:after {
  //  content: '';
  //  display: inline-block;
  //  width: 100%;
  //  height: 20px;
  //}

  .top-tabs {
    min-height: 78px;
    box-sizing: border-box;
    padding: 30px 0 15px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    border-bottom: 1px solid #E9E9E9;

    .tabs {
      display: flex;
      margin-right: 40px;
      font-size: 14px;

      .tab-item {
        cursor: pointer;
        color: #333333;
        font-weight: 500;
        margin-right: 40px;

        &.router-link-exact-active, &:hover {
          color: #466AFF;
        }
      }
    }

    .right {
      display: flex;
      align-items: center;

      .search-input {
        width: 300px;
        box-sizing: border-box;

        ::v-deep(.el-input__inner) {
          border-color: #D0D0D0;
          border-radius: 4px;
        }
      }

      .operations {
        margin-left: 40px;
        display: flex;

        .button {
          display: flex;
          align-items: center;
          cursor: pointer;
          font-size: 14px;
          box-sizing: border-box;
          width: 97px;
          height: 32px;
          color: #333333;
          border: 1px solid transparent;
          border-radius: 4px;

          &:nth-child(1) {
            border: 1px solid #D0D0D0;
          }

          &.jm-icon-button-add::before {
            font-size: 8px;
            margin: 0 5px;
          }
        }

        .code-assembly-line {
          background-color: #FAFBFC;
          margin-right: 10px;
        }

        .graph-assembly-line {
          color: #FFFFFF;
          background-color: #466AFF;
        }
      }
    }
  }

  .content {
    // 密钥命名空间
    ::v-deep(.secret-key-ns-manager) {
      padding: 0;

      .menu-bar button.add,
      .content .item .local-item {
        min-width: 288px;
      }

      .menu-bar button.add {
        margin-top: 20px;
      }
    }

    // 密钥管理页面
    ::v-deep(.secret-key-sk-manager) {
      .namespace {
        margin: 20px 6px;
      }

      .keys {
        padding: 0;
      }

      .menu-bar button.add,
      .content .item {
        min-width: 288px;
      }
    }

    // 代码编辑页面
    ::v-deep(.project-editor) {
      margin-bottom: 0;
    }
  }
}
</style>
<style lang="less">
body {
  background-color: #fff;
}
</style>
