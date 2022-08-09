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
    <div class="content" ref="contentRef">
      <router-view v-slot="{ Component }">
        <component :is="Component" :keyword="key" v-model="flag" v-model:create-type="creatType"/>
      </router-view>
    </div>
  </div>
</template>

<script lang='ts'>
import { computed, defineComponent, getCurrentInstance, onBeforeUnmount, onMounted, provide, ref } from 'vue';
import { useRoute } from 'vue-router';
import _throttle from 'lodash/throttle';
import { injectGlobal } from '@emotion/css';

export default defineComponent({
  setup() {
    const { proxy } = getCurrentInstance() as any;
    const route = useRoute();
    const loadMain = ref<boolean>(true);
    const reloadMain = () => {
      loadMain.value = false;
      proxy.$nextTick(() => (loadMain.value = true));
    };
    const contentRef = ref();
    provide('reloadMain', reloadMain);
    const observer = new ResizeObserver(
      _throttle(() => {
        const height: string = (contentRef.value.offsetHeight + 60 + (route.name === 'index' ? 40 : 0)).toString();
        window.parent.postMessage(JSON.stringify({ height }), '*');
      }, 800),
    );
    onMounted(() => {
      if (window.top !== window) {
        observer.observe(contentRef.value);
      }
      // 动态导入全局entry主题
      injectGlobal(`
        .el-overlay.is-message-box {
          padding-top: 100px;
          background-color: transparent;

          &::after {
            height: 0;
          }
        }
      `);
    });
    onBeforeUnmount(() => {
      observer.disconnect();
    });
    const currentTab = ref<number>(0);
    // 是否展示顶部导航
    const isShowTop = computed<boolean>(() => !((route.name === 'create-project') || (route.name === 'update-project') || (route.name === 'manage-secret-key')));
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
      contentRef,
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
  width: 1158px;
  margin: 0 auto;
  position: relative;

  //&:after {
  //  content: '';
  //  display: inline-block;
  //  width: 100%;
  //  height: 20px;
  //}

  .top-tabs {
    height: 60px;
    box-sizing: border-box;
    padding-bottom: 20px;
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
          //color: #466AFF;
          color: #096DD9;
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
          //background-color: #466AFF;
          background-color: #096DD9;
        }
      }
    }
  }

  .content {
    // 密钥命名空间
    ::v-deep(.secret-key-ns-manager) {
      padding: 30px 0 0;
      min-height: 600px;

      .add,
      .vault-item {
        min-width: 277px;
      }

      .menu-bar button.add {
        margin-top: 20px;
      }
    }

    // 密钥管理页面
    ::v-deep(.secret-key-sk-manager) {
      min-height: 600px;

      .namespace {
        padding: 0 0 10px;
      }

      .keys {
        padding: 0;

        .content {
          .add,
          .item {
            min-width: 277px;
          }
        }
      }
    }

    // 代码编辑页面
    ::v-deep(.project-editor) {
      margin-bottom: 0;
    }

    // 外部参数页面
    ::v-deep(.ext-param) {
      padding: 20px 0;
      min-height: 600px;
      margin-bottom: 0;

      .ext-content {
        .add-param, .ext-param-card {
          min-width: 277px;
          margin: 0.8% 0.5% 0 0.5%;
        }
      }
    }

    // 代码项目编辑页
    ::v-deep(.project-editor) {

      .dsl-editor-entry, .dsl-editor {
        height: 500px;
      }
    }

    //// 修改dialog蒙层颜色
    ::v-deep(.el-overlay) {
      background-color: transparent;
    }

    ::v-deep(.project-preview-dialog) {
      .el-dialog.entry {
        border: none;

        .el-dialog__header {
          box-sizing: border-box;
          padding: 10px 0 0 8px;
          background-color: rgba(255, 255, 255, .6);
          border-bottom: none;
          height: 45px;

          .el-dialog__title {
            color: #082340;
          }
        }

        .el-dialog__body {
          border: 1px solid #B9CFE6;
          box-shadow: 0 0 16px #DDE6EC;
        }
      }
    }
  }
}
</style>
<style lang="less">
body {
  background-color: #fff;
}
</style>
