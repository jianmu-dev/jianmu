<template>
  <div class="webhook-drawer-container">
    <jm-drawer
      v-model="drawerVisible"
      title="Webhook"
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
        <div class="table-container">
          <div class="table-title">请求列表</div>
          <div class="table-content" ref="scrollRef" v-scroll.current="btnDown">
            <jm-table :data="webhookRequestList" :row-key="rowkey">
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
                    <div class="retry" @click="retry(scope.row.id)">重试</div>
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
            <div v-else>
              <span class="bottom">没有更多了</span>
            </div>
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
          <jm-log-viewer filename="webhook.txt" :value="webhookLog" />
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
import { getWebhookList, getWebhookUrl, retryWebRequest } from '@/api/trigger';
import { IWebRequestVo } from '@/api/dto/trigger';
import { datetimeFormatter } from '@/utils/formatter';
import { IPageVo } from '@/api/dto/common';
import { START_PAGE_NUM, DEFAULT_PAGE_SIZE } from '@/utils/constants';

export default defineComponent({
  props: {
    modelValue: {
      type: Boolean,
    },
    currentProjectId: {
      type: String,
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
    const scrollRef = ref<HTMLDivElement>();
    // webhookUrl链接
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
      pageNum: START_PAGE_NUM,
      pageSize: DEFAULT_PAGE_SIZE,
      projectId: '',
    });
    // 请求所有数据
    const webhookRequestData = ref<IPageVo<IWebRequestVo>>({
      total: 0,
      pages: 0,
      list: [],
    });
    // 请求列表
    const webhookRequestList = ref<IWebRequestVo[]>([]);
    // 日志
    const webhookLog = ref<string>('');
    // 监听抽屉切换
    watch(
      () => props.modelValue,
      () => (drawerVisible.value = props.modelValue)
    );
    // 分页返回webhook请求列表
    // listState列表的状态，push/cover
    const getWebhookRequestList = async (listState: string) => {
      const currentScorllTop = scrollRef.value?.scrollTop || 0;
      try {
        webhookRequestData.value = await getWebhookList(
          webhookRequestParams.value
        );
        // 判断是否有下一页
        webhookRequestData.value.pages > webhookRequestParams.value.pageNum
          ? (firstLoading.value = false)
          : (firstLoading.value = true);
        // 追加-加载更多
        if (listState === 'push') {
          // 数据追加
          webhookRequestList.value.push(...webhookRequestData.value.list);
        }
        // 重试-覆盖旧数据
        if (listState === 'cover') {
          webhookRequestList.value = webhookRequestData.value.list;
        }

        nextTick(() => {
          if (!scrollRef.value) {
            return;
          }
          scrollRef.value.scrollTop = currentScorllTop;
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
        const { webhook: webhookUrl } = await getWebhookUrl(
          webhookRequestParams.value.projectId
        );
        webhook.value = webhookUrl;
      } catch (err) {
        proxy.$throw(err, proxy);
      }
    };
    // 监听项目id+请求
    watch(
      () => props.currentProjectId,
      () => {
        // 还原页码
        webhookRequestParams.value.pageNum = START_PAGE_NUM;
        webhookRequestList.value = [];
        // 获取webhook请求列表
        webhookRequestParams.value.projectId = props.currentProjectId as string;
        getWebhookRequestList('cover');
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
    // 重试api
    const retryRequest = async (id: string) => {
      try {
        webhookRequestParams.value.pageNum = START_PAGE_NUM;
        proxy.$success('重试成功');
        await retryWebRequest(id);
        // 旧数据覆盖新数据
        getWebhookRequestList('cover');
      } catch (err) {
        proxy.$throw(err, proxy);
      }
    };
    // 重试
    let msg = '<div>确定要重试吗?</div>';
    const retry = (id: string) => {
      proxy
        .$confirm(msg, '重试', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'info',
          dangerouslyUseHTMLString: true,
        })
        .then(() => retryRequest(id));
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
      if (
        webhookRequestData.value!.pages > webhookRequestParams.value.pageNum
      ) {
        webhookRequestParams.value.pageNum++;
        bottomLoading.value = true;
        getWebhookRequestList('push');
      }
    };
    const rowkey = (row: any) => {
      return row.id;
    };
    return {
      rowkey,
      scrollRef,
      drawerVisible,
      link,
      copy,
      // 表单数据
      webhookRequestList,
      closeDrawer,
      retry,
      seePayload,
      payloadDialogVisible,
      // 日志
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
  .ellipsis {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
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
          .jm-icon-input-hook {
            width: 760px;
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
      .table-content {
        max-height: calc(100vh - 341px);
        overflow-y: auto;
        border-radius: 4px;
        border: 1px solid #ecedf4;
        ::v-deep(.el-table) {
          background: #fff;
          font-size: 14px;
          color: #082340;
          overflow: visible;
          height: auto;
          &::before {
            height: 0;
          }
          td {
            border-bottom: 1px solid #ecedf4;
            border-right: 1px solid #ecedf4;
          }
          th {
            text-align: center;
            border-right: 1px solid #ecedf4;
            font-weight: 500;
          }
          th:last-of-type {
            border-right: none;
          }
          tr {
            height: 56px;
          }
          tr {
            td:last-of-type {
              border-right: none;
            }
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
          }
        }
      }
      // 显示更多
      .bottom {
        margin: 15px 0;
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
