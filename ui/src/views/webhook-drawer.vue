<template>
  <div class="webhook-drawer-container">
    <jm-drawer
      v-model="drawerVisible"
      title="webhook"
      :size="953"
      direction="rtl"
      @close="closeDrawer"
    >
      <div class="webhook-drawer">
        <div class="webhook-link-container">
          <div class="link-tips">可以通过调用以下Webhook地址来触发流程执行</div>
          <div class="link-container">
            <div class="link-address">
              <jm-tooltip :content="link" placement="top">
                <div class="jm-icon-input-hook ellipsis">{{ link }}</div>
              </jm-tooltip>
            </div>
            <div class="copy-link-address">
              <jm-button type="primary" @click="copy">复制链接</jm-button>
            </div>
          </div>
        </div>
        <div class="table-container">
          <div class="table-title">请求列表</div>
          <jm-table :data="tableData" border>
            <jm-table-column prop="laiyuan" label="来源"></jm-table-column>
            <jm-table-column
              prop="timed"
              label="请求时间"
              align="center"
            ></jm-table-column>
            <jm-table-column
              prop="state"
              label="状态"
              align="center"
            ></jm-table-column>
            <jm-table-column
              prop="message"
              label="错误信息"
              align="center"
            ></jm-table-column>
            <jm-table-column label="操作" align="center">
              <template #default>
                <div class="table-button">
                  <div class="retry" @click="retry">重试</div>
                  <div class="see-payload" @click="seePayload">查看payload</div>
                </div>
              </template>
            </jm-table-column>
          </jm-table>
        </div>
      </div>
      <!-- 查看payload -->
      <jm-dialog v-model="payloadDialogVisible" title="查看payload">
        <div class="payload-content">
          <jm-log-viewer :value="log" />
        </div>
      </jm-dialog>
    </jm-drawer>
  </div>
  <!-- 查看payload弹窗 -->
</template>

<script lang="ts">
import { defineComponent, ref, getCurrentInstance, watch } from 'vue';
import useClipboard from 'vue-clipboard3';

export default defineComponent({
  props: {
    modelValue: {
      type: Boolean,
      require: true,
    },
  },
  emits: ['close-webhook-drawer'],
  setup(props, { emit }) {
    const { proxy } = getCurrentInstance() as any;
    const { toClipboard } = useClipboard();
    // 抽屉控制
    const drawerVisible = ref<boolean>(false);
    // 弹窗控制
    const payloadDialogVisible = ref<boolean>(false);
    watch(
      () => props.modelValue,
      () => (drawerVisible.value = props.modelValue)
    );
    const link = ref<string>('http://xxx.xxx.xxx/xxx/xxx');
    // 模拟日志
    let log = '';
    for (let i = 1; i <= 10; i++) {
      log += `第${i}行日志\n`;
    }
    // 表单模拟数据
    const tableData = ref([
      {
        laiyuan: 'ci.jiamu.run/event',
        timed: '2021-11-18 18:00:00',
        state: '成功',
        message: '密码不正确',
      },
      {
        laiyuan: 'ci.jiamu.run/event',
        timed: '2021-11-18 18:00:00',
        state: '成功',
        message: '密码不正确',
      },
      {
        laiyuan: 'ci.jiamu.run/event',
        timed: '2021-11-18 18:00:00',
        state: '成功',
        message: '密码不正确',
      },
      {
        laiyuan: 'ci.jiamu.run/event',
        timed: '2021-11-18 18:00:00',
        state: '成功',
        message: '密码不正确',
      },
    ]);
    // 复制
    const copy = async () => {
      if (!link.value) {
        return;
      }
      try {
        await toClipboard(link.value);
        proxy.$success('复制成功');
      } catch (err) {
        proxy.$error('复制失败，请手动复制');
        console.error(err);
      }
    };
    // 关闭drawer
    const closeDrawer = () => {
      emit('close-webhook-drawer', false);
    };
    // 重试
    let msg = '<div>确定要重试吗?</div>';
    const retry = () => {
      proxy.$confirm(msg, '重试', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info',
        dangerouslyUseHTMLString: true,
      });
    };
    // 查看payload
    const seePayload = () => {
      payloadDialogVisible.value = true;
    };
    return {
      drawerVisible,
      link,
      copy,
      // 模拟表格数据
      tableData,
      closeDrawer,
      retry,
      seePayload,
      payloadDialogVisible,
      // 模拟日志
      log,
    };
  },
});
</script>

<style scoped lang="less">
.webhook-drawer-container {
  ::v-deep(.el-drawer) {
    .el-drawer__header {
      > span::before {
        font-family: 'jm-icon-input';
        content: '\e803';
        margin-right: 10px;
        color: #6b7b8d;
        font-size: 20px;
        vertical-align: bottom;
      }
    }
  }
  .webhook-drawer {
    height: 100%;
    background: #eff4f9;
    box-sizing: border-box;
    padding: 20px 25px 0 25px;
    // webhook地址
    .webhook-link-container {
      width: 905px;
      height: 125px;
      background: #fff;
      box-sizing: border-box;
      padding: 20px 30px;
      margin-bottom: 20px;
      .link-tips {
        font-size: 14px;
        color: #082340;
        margin-bottom: 10px;
      }
      .link-container {
        height: 36px;
        display: flex;
        align-items: center;
        .link-address {
          width: 760px;
          height: 40px;
          line-height: 40px;
          background: #f6f8fb;
          font-size: 16px;
          color: #082340;
          margin-right: 10px;
          border-radius: 2px;
          > :first-child:before {
            margin: 0 10px;
            font-size: 16px;
            color: #6b7b8d;
          }
          a {
            color: inherit;
            text-decoration: none;
            &:hover {
              text-decoration: underline;
            }
          }
        }
        .copy-link-address {
          button {
            height: 36px;
            border-radius: 2px;
            border: none;
          }
        }
      }
    }
    // 表格
    .table-container {
      background: #fff;
      box-sizing: border-box;
      padding: 20px;
      .table-title {
        margin-bottom: 20px;
        font-size: 14px;
        color: #082340;
      }
      ::v-deep(.el-table) {
        background: #fff;
        font-size: 14px;
        color: #082340;
        th {
          text-align: center;
          border-right: none;
          font-weight: 500;
        }
        tr {
          height: 56px;
        }
        .table-button {
          display: flex;
          justify-content: center;
          .retry,
          .see-payload {
            font-size: 14px;
            color: #096dd9;
            font-weight: 600;
            cursor: pointer;
          }
          .retry {
            margin-right: 20px;
          }
          // 状态
          .success {
            color: red;
          }
          .fail {
            color: green;
          }
        }
      }
    }
  }
  .payload-content {
    height: 200px;
  }
}
</style>
