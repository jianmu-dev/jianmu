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
                <div class="jm-icon-input-hook ellipsis">
                  {{ link }}
                </div>
              </jm-tooltip>
            </div>
            <div class="copy-link-address">
              <jm-button type="primary" @click="copy">复制链接</jm-button>
            </div>
          </div>
        </div>
        <div class="table-container" v-loading="bottomLoading">
          <div class="table-title">请求列表</div>
          <jm-table
            :data="webhookRequestList"
            border
            v-scroll.current="btnDown"
          >
            <jm-table-column prop="userAgent" label="来源"></jm-table-column>
            <jm-table-column prop="timed" label="请求时间" align="center">
              <template #default="scope">
                <div>{{ datetimeFormatter(scope.row.requestTime) }}</div>
              </template></jm-table-column
            >
            <jm-table-column prop="statusCode" label="状态" align="center">
              <template #default="scope">
                <div
                  v-if="scope.row.statusCode === 'OK'"
                  style="color: #10c2c2"
                >
                  成功
                </div>
                <div v-else style="color: red">失败</div>
              </template>
            </jm-table-column>
            <jm-table-column
              prop="errorMsg"
              label="错误信息"
              align="center"
            ></jm-table-column>
            <jm-table-column label="操作" align="center">
              <template #default="scope">
                <div class="table-button">
                  <div class="retry" @click="retry">重试</div>
                  <div class="see-payload" @click="seePayload(scope.row.id)">
                    查看payload
                  </div>
                </div>
              </template>
            </jm-table-column>
          </jm-table>
          <div v-if="noMore && !firstLoading" @click="btnDown" class="bottom">
            <span>显示更多</span>
            <i class="btm-down" :class="{ 'btn-loading': bottomLoading }"></i>
          </div>
        </div>
      </div>
      <!-- 查看payload -->
      <jm-dialog
        v-model="payloadDialogVisible"
        title="查看payload"
        destroy-on-close
      >
        <div class="payload-content">
          <jm-log-viewer :value="webhookLog" />
        </div>
      </jm-dialog>
    </jm-drawer>
  </div>
</template>

<script lang="ts">
import {
  defineComponent,
  ref,
  getCurrentInstance,
  watch,
  computed,
  nextTick,
} from 'vue';
import useClipboard from 'vue-clipboard3';
import { getWebhookList, getWebhookUrl } from '@/api/trigger';
import { IWebRequestVo } from '@/api/dto/trigger';
import { IWebhookRequestTable } from '@/model/modules/trigger';
import { datetimeFormatter } from '@/utils/formatter';
import { IPageVo } from '@/api/dto/common';

export default defineComponent({
  props: {
    modelValue: {
      type: Boolean,
      require: true,
    },
    currentProjectId: {
      type: String,
      required: true,
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
    // 显示更多
    const noMore = ref<boolean>(true);
    const firstLoading = ref<boolean>(false);
    const bottomLoading = ref<boolean>(false);
    // 模拟链接
    const webhook = ref<string>();
    const link = computed<string | undefined>(
      () =>
        webhook.value &&
        `${window.location.protocol}//${window.location.host}${webhook.value}`
    );
    // 请求参数
    const webhookRequestParams = ref<{
      pageNum: number;
      pageSize: number;
      projectId: string;
    }>({
      pageNum: 1,
      pageSize: 1,
      projectId: '',
    });
    // 请求所有数据
    const webhookRequestData = ref<IPageVo<IWebRequestVo>>();
    // 请求列表
    const webhookRequestList = ref<IWebhookRequestTable[]>([]);
    // 模拟日志
    const webhookLog = ref<string>('');
    // 监听抽屉切换
    watch(
      () => props.modelValue,
      () => (drawerVisible.value = props.modelValue)
    );
    // 分页返回webhook请求列表
    const getWebhookRequestList = async () => {
      try {
        webhookRequestData.value = await getWebhookList(
          webhookRequestParams.value
        );
        console.log('请求下来的数据', webhookRequestData.value);
        // 判断是否有下一页
        webhookRequestData.value.total > webhookRequestList.value.length
          ? (firstLoading.value = false)
          : (firstLoading.value = true);
        // 数据追加
        webhookRequestData.value.list?.forEach(item => {
          webhookRequestList.value.push(item);
        });
        // 还原状态
        bottomLoading.value = false;
      } catch (err) {
        proxy.$throw(err, proxy);
      }
    };
    // webhookUrl
    const getWebhookUrlRequest = async () => {
      try {
        const { webhook: webhookUri } = await getWebhookUrl(
          webhookRequestParams.value.projectId
        );
        webhook.value = webhookUri;
      } catch (err) {
        proxy.$throw(err, proxy);
      }
    };
    // 监听项目id+请求
    watch(
      () => props.currentProjectId,
      () => {
        webhookRequestList.value = [];
        // 获取webhook请求列表
        webhookRequestParams.value.projectId = props.currentProjectId;
        getWebhookRequestList();
        // 获取webhookUrl
        getWebhookUrlRequest();
      }
    );
    // 一键复制
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
    const seePayload = (id: string) => {
      const { payload } = webhookRequestList.value.find(
        item => item.id === id
      ) as IWebRequestVo;
      webhookLog.value = JSON.stringify(JSON.parse(payload), null, 2);
      nextTick(() => {
        payloadDialogVisible.value = true;
      });
    };

    // 显示更多
    const btnDown = () => {
      // 如果还有下一页才会触发
      if (webhookRequestData.value.hasNextPage) {
        webhookRequestParams.value.pageNum++;
        console.log(webhookRequestParams.value);
        bottomLoading.value = true;
        getWebhookRequestList();
      }
    };
    return {
      drawerVisible,
      link,
      copy,
      // 模拟表格数据
      // 表单数据
      webhookRequestList,
      closeDrawer,
      retry,
      seePayload,
      payloadDialogVisible,
      // 模拟日志
      webhookLog,
      // 显示更多
      noMore,
      firstLoading,
      bottomLoading,
      btnDown,
      datetimeFormatter,
    };
  },
});
</script>

<style scoped lang="less">
.webhook-drawer-container {
  ::v-deep(.el-drawer) {
    // 图标
    .el-drawer__header {
      > span::before {
        font-family: 'jm-icon-input';
        content: '\e803';
        margin-right: 10px;
        color: #6b7b8d;
        font-size: 20px;
        vertical-align: bottom;
        position: relative;
        top: 1px;
      }
    }
  }
  // webhook抽屉
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
      padding: 30px 20px;
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
          width: 700px;
          height: 40px;
          line-height: 40px;
          background: #f6f8fb;
          font-size: 16px;
          color: #082340;
          margin-right: 10px;
          border-radius: 2px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
          > :first-child:before {
            display: inline-block;
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
          .jm-icon-input-hook {
            width: 700px;
            display: flex;
            align-items: center;
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
      height: 530px;
      .table-title {
        margin-bottom: 20px;
        font-size: 14px;
        color: #082340;
      }
      .success {
        color: green;
      }
      .fail {
        color: red;
      }
      ::v-deep(.el-table) {
        background: #fff;
        font-size: 14px;
        color: #082340;
        height: 420px;
        // height: calc(100vh - 112px);
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
      // 显示更多
      .bottom {
        margin-top: 25px;
        color: #7b8c9c;
        font-size: 14px;
        display: flex;
        align-items: center;
        justify-content: center;
        cursor: pointer;

        .btm-down {
          width: 16px;
          height: 16px;
          background-image: url('@/assets/svgs/node-library/drop-down.svg');
          margin-left: 6px;
        }

        .btm-down.btn-loading {
          animation: rotate 1s cubic-bezier(0.58, -0.55, 0.38, 1.43) infinite;
          background-image: url('@/assets/svgs/node-library/loading.svg');
        }

        @keyframes rotate {
          0% {
            transform: rotate(0deg);
          }
          100% {
            transform: rotate(360deg);
          }
        }
      }
    }
  }
  // 查看paload
  ::v-deep(.el-dialog) {
    .el-dialog__title {
      font-size: 16px;
      color: #082340;
      font-weight: 500;
    }
    .el-dialog__body {
      border: none;
      padding: 0px 20px 25px 20px;
    }
    .payload-content {
      height: 500px;
    }
  }
}
</style>
