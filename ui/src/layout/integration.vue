<template>
  <div class="integration">
    <div class="top-tabs" v-if="isShowTop">
      <div class="tabs left">
        <router-link :to="{ name: 'index' }" @click="currentTab = 0" class="tab-item"> 流水线</router-link>
        <router-link :to="{ name: 'ext-param' }" @click="currentTab = 1" class="tab-item"> 外部参数</router-link>
        <router-link :to="{ name: 'secret-key' }" @click="currentTab = 2" class="tab-item"> 密钥管理</router-link>
        <div class="divider"></div>
        <a class="using-docs-btn" href="https://forum.gitlink.org.cn/forums/7487/detail" target="_blank">
          <span>使用文档</span>
          <i class="jump-icon"></i>
        </a>
      </div>
      <div class="right" v-show="$route.name === 'index'">
        <jm-input
          placeholder="搜索流水线"
          class="search-input"
          size="small"
          v-model="keyWord"
          @change="search"
          clearable
        >
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
        <component :is="Component" :keyword="key" v-model="flag" v-model:create-type="creatType" />
        <div class="link" v-if="isShowTop">引擎功能由<a href="https://jianmu.dev" target="_blank">建木</a>提供</div>
      </router-view>
    </div>
  </div>
</template>

<script lang="ts">
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
        const height: string = (contentRef.value.offsetHeight + 80).toString();
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
    const isShowTop = computed<boolean>(
      () => !(route.name === 'create-project' || route.name === 'update-project' || route.name === 'manage-secret-key'),
    );
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

<style lang="less" scoped>
.integration {
  width: 1158px;
  padding: 20px;
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
    border-bottom: 1px solid #e9e9e9;

    .tabs {
      display: flex;
      margin-right: 40px;
      font-size: 14px;

      .divider {
        width: 1px;
        height: 20px;
        background-color: #d0d0d0;
        margin-right: 30px;
      }

      .using-docs-btn {
        font-weight: 500;
        color: #333333;
        display: flex;
        align-items: center;

        &:hover {
          color: #096dd9;

          i {
            background: url('@/assets/svgs/index/jump-active.svg') no-repeat 100%;
          }
        }

        i {
          margin-left: 5px;
          width: 16px;
          height: 16px;
          background: url('@/assets/svgs/index/jump.svg') no-repeat 100%;
        }
      }

      .tab-item {
        cursor: pointer;
        color: #333333;
        font-weight: 500;
        margin-right: 30px;

        &.router-link-exact-active,
        &:hover {
          //color: #466AFF;
          color: #096dd9;
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
          border-color: #d0d0d0;
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
            border: 1px solid #d0d0d0;
          }

          &.jm-icon-button-add::before {
            font-size: 8px;
            margin: 0 5px;
          }
        }

        .code-assembly-line {
          background-color: #fafbfc;
          margin-right: 10px;
        }

        .graph-assembly-line {
          color: #ffffff;
          //background-color: #466AFF;
          background-color: #096dd9;
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
        .add-param,
        .ext-param-card {
          min-width: 277px;
          margin: 0.8% 0.5% 0 0.5%;
        }
      }

      .el-overlay {
        // 解决弹窗可以上下滚动
        overflow: hidden;
        // 调整上间距防止弹窗底部被截
        .el-dialog.entry {
          margin-top: 70px !important;
        }
      }
    }

    // 代码项目编辑页
    ::v-deep(.project-editor) {
      .dsl-editor-entry,
      .dsl-editor {
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
          width: 100%;
          position: absolute;
          top: 0;
          z-index: 20;
          box-sizing: border-box;
          display: flex;
          align-items: center;
          border-bottom: none;
          height: 60px;
          justify-content: center;

          .el-dialog__title {
            color: #082340;
            width: 568px;
            text-align: center;
          }

          .el-dialog__headerbtn {
            top: 18px;
          }
        }

        .el-dialog__body {
          border: 1px solid #b9cfe6;
          box-shadow: 0 0 16px #dde6ec;
        }
      }
    }
  }

  .link {
    margin-top: 35px;
    text-align: right;
    font-size: 12px;
    font-weight: 400;
    color: #6b7b8d;

    a {
      font-weight: 600;
      color: #6b7b8d;

      &:hover {
        color: #096dd9;
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
