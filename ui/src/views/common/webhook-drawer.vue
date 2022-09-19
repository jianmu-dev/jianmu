<template>
  <div class="webhook-drawer-container">
    <jm-drawer
      v-model="drawerVisible"
      title="Webhook"
      :size="953"
      direction="rtl"
      @closed="closeDrawer"
      destroy-on-close
    >
      <div class="webhook-drawer">
        <!-- 项目名称 -->
        <div class="project-name">{{ currentProject }}</div>
        <div class="webhook-link-container">
          <div class="link-tips">可以通过调用以下Webhook地址来触发流程执行</div>
          <div class="link-container">
            <div class="link-address">
              <!-- 左侧图标 -->
              <i class="jm-icon-input-hook"></i>
              <!-- 右侧链接 -->
              <jm-text-viewer :value="link" class="webhook-link"/>
            </div>
            <div class="copy-link-address">
              <jm-button type="primary" @click="copy">复制链接</jm-button>
            </div>
          </div>
        </div>
        <div class="table-title">
          <div class="title-container">
            <div class="title">请求列表</div>
            <jm-tooltip placement="top">
              <template #content>
                若无对应的触发记录，可到上游webhook管理中，<br/>
                查看请求是否发送成功
              </template>
              <i class="jm-icon-button-help"></i>
            </jm-tooltip>
          </div>
          <jm-tooltip content="刷新" placement="bottom">
            <jm-button
              :class="{ 'jm-icon-button-refresh': true, 'doing': refreshFlag}"
              :disabled="refreshFlag"
              @click="refresh"
            ></jm-button>
          </jm-tooltip>
        </div>
        <div class="table-container" v-loading="tableLoading">
          <jm-scrollbar
            ref="webhookDrawerRef"
            :height="height"
            class="webhook-drawer-ref"
          >
            <div
              class="table-content"
              ref="scrollRef"
              v-if="refreshVisible"
              v-scroll="{
                throttle: 800,
                loadMore: btnDown,
                scrollableEl,
              }"
            >
              <jm-table :data="webhookRequestList" :row-key="rowkey">
                <jm-table-column
                  prop="userAgent"
                  label="来源"
                ></jm-table-column>
                <jm-table-column prop="timed" label="请求时间" align="center">
                  <template #default="scope">
                    <div>{{ datetimeFormatter(scope.row.requestTime) }}</div>
                  </template>
                </jm-table-column
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
                <jm-table-column label="错误信息">
                  <template #default="scope">
                    <jm-text-viewer :value="scope.row.errorMsg||''"/>
                  </template>
                </jm-table-column>
                <jm-table-column label="操作" align="center">
                  <template #default="scope">
                    <div class="table-button">
                      <div class="retry" @click="retry(scope.row.id)">重试</div>
                      <div
                        class="see-payload"
                        @click="seePayload(scope.row.id)"
                      >
                        详情
                      </div>
                    </div>
                  </template>
                </jm-table-column>
              </jm-table>
              <!-- 显示更多 -->
              <div class="load-more">
                <jm-load-more
                  :state="loadState"
                  :load-more="btnDown"
                ></jm-load-more>
              </div>
            </div>
          </jm-scrollbar>
        </div>
      </div>
      <!-- 查看payload -->
      <jm-dialog
        v-model="payloadDialogVisible"
        title="查看详情"
        @closed="dialogClose"
        destroy-on-close
        width="1000px"
        top="5vh"
      >
        <div class="tab-container">
          <div :class="{ active: payloadTab }" @click="toPayload">触发器</div>
          <div :class="{ active: !payloadTab }" @click="toTrigger">Payload</div>
        </div>
        <!-- 查看payload -->
        <div class="payload-content" v-if="!payloadTab" v-loading="payloadLoading">
          <jm-log-viewer v-if="webhookLog" filename="webhook.txt" :value="webhookLog"/>
        </div>
        <!-- 触发器 -->
        <div v-else class="trigger-content" v-loading="triggerParamsLoading">
          <jm-scrollbar>
            <div style="padding:20px;">
              <!-- 参数列表 -->
              <div class="trigger-title">参数列表</div>
              <jm-table class="trigger-table" :data="webhookParamsDetail?.param">
                <jm-table-column label="参数唯一标识">
                  <template #default="scope">
                    <jm-text-viewer :value="scope.row.name"/>
                  </template>
                </jm-table-column>
                <jm-table-column label="参数类型" width="200px" prop="type">
                </jm-table-column>
                <jm-table-column label="参数值" prop="value">
                  <template #default="scope">
                    <div v-if="scope.row.type === ParamTypeEnum.SECRET">
                      <!-- 密钥类型切换 -->
                      <div class="hide-container" v-if="secretVisible">
                        <span>********************</span>
                        <i
                          class="hide-secret jm-icon-input-visible"
                          @click="hideSecret"
                        ></i>
                      </div>
                      <div class="display-container" v-else>
                        <template v-if="scope.row.value">
                          <div class="param-value"
                               :style="{maxWidth:maxWidthRecord[scope.row.value]?`${maxWidthRecord[scope.row.value]}px`: '100%'}">
                            <jm-text-viewer v-if="scope.row.valueType !== ParamTypeEnum.SECRET"
                                            :value="scope.row.value"
                                            @loaded="({contentMaxWidth})=>getTotalWidth(contentMaxWidth,scope.row.value)"
                                            class="value"
                            >
                            </jm-text-viewer>
                            <template v-else>
                              {{ scope.row.value }}
                            </template>
                          </div>
                        </template>
                        <i
                          class="display-secret jm-icon-input-invisible"
                          @click="displaySecret"
                        ></i>
                      </div>
                    </div>
                    <template v-else>
                      <param-value
                        :value="scope.row.value"
                        :type="scope.row.valueType"
                      />
                    </template>
                  </template>
                </jm-table-column>
              </jm-table>
              <!-- 认证 -->
              <div class="verify-title">认证</div>
              <jm-table class="verify-table" :data="webhookAuth">
                <jm-table-column label="token" prop="token"></jm-table-column>
                <jm-table-column label="value" prop="value"></jm-table-column>
              </jm-table>
              <!-- 匹配认证 -->
              <div class="matching-title">匹配认证</div>
              <div class="matching-container" v-if="webhookParamsDetail?.only">
                {{ webhookParamsDetail?.only }}
              </div>
              <div class="matching-container matching-null" v-else>暂无数据</div>
            </div>
          </jm-scrollbar>
        </div>
      </jm-dialog>
    </jm-drawer>
  </div>
</template>

<script lang="ts">
import { computed, defineComponent, getCurrentInstance, nextTick, onUpdated, ref } from 'vue';
import useClipboard from 'vue-clipboard3';
import { getPayloadParams, getWebhookList, getWebhookParams, retryWebRequest } from '@/api/trigger';
import { IWebhookAuthVo, IWebhookParamVo, IWebRequestVo } from '@/api/dto/trigger';
import { datetimeFormatter } from '@/utils/formatter';
import { IPageVo } from '@/api/dto/common';
import { DEFAULT_PAGE_SIZE, START_PAGE_NUM } from '@/utils/constants';
import { fetchTriggerWebhook } from '@/api/view-no-auth';
import { ElScrollbar } from 'element-plus';
import { StateEnum } from '@/components/load-more/enumeration';
import { ParamTypeEnum } from '@/api/dto/enumeration';
import JmTextViewer from '@/components/text-viewer/index.vue';
import ParamValue from '@/views/common/param-value.vue';

export default defineComponent({
  components: { JmTextViewer, ParamValue },
  props: {
    webhookVisible: {
      type: Boolean,
    },
    currentProjectId: {
      type: String,
    },
    currentProjectName: {
      type: String,
    },
  },
  emits: ['update:webhookVisible'],
  setup(props, { emit }) {
    const { proxy } = getCurrentInstance() as any;
    const { toClipboard } = useClipboard();

    // 抽屉控制
    const drawerVisible = ref<boolean>(props.webhookVisible);
    // 弹窗控制
    const payloadDialogVisible = ref<boolean>(false);
    // 显示更多
    const scrollRef = ref<HTMLDivElement>();
    const webhookDrawerRef = ref<InstanceType<typeof ElScrollbar>>();
    const loadState = ref<StateEnum>(StateEnum.NONE);
    const payloadTab = ref<boolean>(true);
    // 列表id
    const webhookListId = ref<string>('');
    // webhook参数详情
    const webhookParamsDetail = ref<IWebhookParamVo>();
    const webhookAuth = ref<IWebhookAuthVo[]>([]);
    // 密钥类型显隐
    const secretVisible = ref<boolean>(true);
    const maxWidthRecord = ref<Record<string, number>>({});
    // 刷新表格
    const refreshVisible = ref<boolean>(true);
    const refreshFlag = ref<boolean>(false);
    const triggerParamsLoading = ref<boolean>(false);
    const payloadLoading = ref<boolean>(false);
    // webhook请求列表滚动
    const scrollableEl = () => {
      return webhookDrawerRef.value?.scrollbar.firstElementChild;
    };
    const h =
      (window.innerHeight ||
        document.documentElement.clientHeight ||
        document.body.clientHeight) - 335;
    const scrollHeight = ref<number>(h);
    const height = computed<number>({
      get() {
        return scrollHeight.value;
      },
      set(value) {
        scrollHeight.value = value;
      },
    });
    // webhookUrl链接
    const webhook = ref<string>();
    const noMoreFlag = ref<boolean>(false);
    // 表格loading
    const tableLoading = ref<boolean>(false);
    const link = computed<string | undefined>(
      () =>
        webhook.value &&
        `${window.location.protocol}//${window.location.host}${webhook.value}`,
    );
    // 当前webhook的项目名
    const currentProject = ref<string>(props.currentProjectName as string);
    // 请求参数
    const webhookRequestParams = ref<{
      pageNum: number;
      pageSize: number;
      projectId: string;
    }>({
      pageNum: START_PAGE_NUM,
      pageSize: DEFAULT_PAGE_SIZE,
      projectId: props.currentProjectId || '',
    });
    // 请求所有数据
    const webhookRequestData = ref<IPageVo<IWebRequestVo>>({
      total: 0,
      pages: 0,
      list: [],
      pageNum: START_PAGE_NUM,
    });
    // 请求列表
    const webhookRequestList = ref<IWebRequestVo[]>([]);
    // 日志
    const webhookLog = ref<string>('');
    // webhookUrl
    const getWebhookUrlRequest = async () => {
      try {
        const { webhook: webhookUrl } = await fetchTriggerWebhook(
          webhookRequestParams.value.projectId,
        );
        webhook.value = webhookUrl;
      } catch (err) {
        proxy.$throw(err, proxy);
      }
    };
    // 分页返回webhook请求列表
    // listState列表的状态，push/cover
    const getWebhookRequestList = async (listState: string) => {
      const currentScorllTop = scrollRef.value?.scrollTop || 0;
      try {
        // 请求表格数据
        webhookRequestData.value = await getWebhookList(
          webhookRequestParams.value,
        );
        // 数据请求成功取消loading
        tableLoading.value = false;
        // 判断是否有下一页
        if (
          webhookRequestData.value.pages > webhookRequestParams.value.pageNum
        ) {
          loadState.value = StateEnum.MORE;
        } else {
          loadState.value = StateEnum.NO_MORE;
        }
        // 如果总数为0就不展示没有更多
        if (webhookRequestData.value.total === 0) {
          // 什么都不显示
          loadState.value = StateEnum.NONE;
        }
        // 追加-加载更多
        if (listState === 'push') {
          // 数据追加
          webhookRequestList.value.push(...webhookRequestData.value.list);
        }
        // 重试-覆盖旧数据
        if (listState === 'cover') {
          webhookRequestList.value = webhookRequestData.value.list;
        }

        await nextTick(() => {
          if (!scrollRef.value) {
            return;
          }
          scrollRef.value.scrollTop = currentScorllTop;
        });
      } catch (err) {
        tableLoading.value = false;
        proxy.$throw(err, proxy);
      }
    };
    // 获取webhook的url和表格数据
    const getWebhookInfo = () => {
      if (drawerVisible.value === props.webhookVisible) {
        return;
      }
      // 打开抽屉
      drawerVisible.value = props.webhookVisible;
      // 更改projectId
      webhookRequestParams.value.projectId = props.currentProjectId as string;
      // 打开抽屉时重新赋值项目名
      currentProject.value = props.currentProjectName as string;
      // 进入抽屉显示loading
      tableLoading.value = true;
      // 打开抽屉时什么状态都没有
      loadState.value = StateEnum.NONE;
      // 获取webhookUrl
      getWebhookUrlRequest();
      // 获取webhook请求列表
      getWebhookRequestList('cover');
    };
    onUpdated(() => getWebhookInfo());
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
      emit('update:webhookVisible', false);
      // 清空数组
      webhookRequestList.value = [];
      // 还原页码
      webhookRequestParams.value.pageNum = START_PAGE_NUM;
    };
    // 重试api
    const retryRequest = async (id: string) => {
      try {
        webhookRequestParams.value.pageNum = START_PAGE_NUM;
        await retryWebRequest(id);
        proxy.$success('重试成功');
        // 旧数据覆盖新数据
        await getWebhookRequestList('cover');
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
    // 获取触发器参数
    const getTriggerParam = async () => {
      try {
        triggerParamsLoading.value = true;
        webhookParamsDetail.value = await getWebhookParams(webhookListId.value);
        // 清空数组
        webhookAuth.value = [];
        // 重新赋值
        webhookParamsDetail.value.auth
          ? webhookAuth.value.push(webhookParamsDetail.value.auth)
          : webhookAuth.value;
      } catch (err) {
        proxy.$throw(err, proxy);
      } finally {
        triggerParamsLoading.value = false;
      }
    };
    // 查看payload
    const seePayload = async (id: string) => {
      webhookListId.value = id;
      await getTriggerParam();
      payloadDialogVisible.value = true;
    };
    // 显示更多
    const btnDown = () => {
      // 如果还有下一页才会触发
      if (
        webhookRequestData.value!.pages > webhookRequestParams.value.pageNum
      ) {
        loadState.value = StateEnum.LOADING;
        webhookRequestParams.value.pageNum++;
        getWebhookRequestList('push');
      }
    };
    const rowkey = (row: any) => {
      return row.id;
    };
    // payload
    const toTrigger = async () => {
      payloadTab.value = false;
      try {
        payloadLoading.value = true;
        const payloadContent = await getPayloadParams(webhookListId.value);
        webhookLog.value = JSON.stringify(JSON.parse(payloadContent.payload), null, 2);
      } catch (err) {
        proxy.$throw(err, proxy);
      } finally {
        payloadLoading.value = false;
      }
    };
    // 触发器
    const toPayload = () => {
      payloadTab.value = true;
      // 还原密钥显示
      secretVisible.value = true;
    };
    // 弹窗关闭
    const dialogClose = () => {
      // 还原tab
      payloadTab.value = true;
      // 还原密钥显示
      secretVisible.value = true;
    };
    // 一键复制
    const copyParam = async (value: string) => {
      if (!value) {
        return;
      }
      try {
        await toClipboard(value);
        proxy.$success('复制成功');
      } catch (err) {
        proxy.$error('复制失败，请手动复制');
        console.error(err);
      }
    };
    // 刷新
    const refresh = async () => {
      refreshFlag.value = true;
      refreshVisible.value = false;
      tableLoading.value = true;
      webhookRequestParams.value.pageNum = START_PAGE_NUM;
      await getWebhookRequestList('cover');
      refreshVisible.value = true;
      tableLoading.value = false;
      refreshFlag.value = false;
    };
    return {
      loadState,
      height,
      scrollableEl,
      webhookDrawerRef,
      rowkey,
      scrollRef,
      drawerVisible,
      link,
      copy,
      copyParam,
      // 表单数据
      webhookRequestList,
      closeDrawer,
      retry,
      seePayload,
      payloadDialogVisible,
      // 日志
      webhookLog,
      btnDown,
      datetimeFormatter,
      noMoreFlag,
      tableLoading,
      // 切换tab
      payloadTab,
      toTrigger,
      toPayload,
      // 请求详情
      webhookParamsDetail,
      webhookAuth,
      // 弹窗关闭
      dialogClose,
      // 密钥类型显隐
      secretVisible,
      hideSecret: () => (secretVisible.value = false),
      displaySecret: () => (secretVisible.value = true),
      // 当前项目名
      currentProject,
      ParamTypeEnum,
      maxWidthRecord,
      getTotalWidth(width: number, ref: string) {
        maxWidthRecord.value[ref] = width;
      },
      refresh,
      refreshVisible,
      refreshFlag,
      triggerParamsLoading,
      payloadLoading,
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
    // 项目名称
    .project-name {
      margin-bottom: 14px;
      font-size: 16px;
      color: #082340;
    }

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
          display: flex;
          // 图标
          .jm-icon-input-hook {
            content: '\e818';
            margin: 0 14px;
          }

          // 链接
          .webhook-link {
            width: 700px;
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

    .table-title {
      height: 16px;
      margin-bottom: 14px;
      font-size: 16px;
      display: flex;
      justify-content: space-between;
      align-items: center;

      .title-container {
        display: flex;

        .title {
          color: #082340;
          margin-right: 5px;
        }

        .jm-icon-button-help {
          min-height: 16px;
          color: #818c9b;
        }
      }

      .jm-icon-button-refresh {
        padding: 0px;
        min-height: 16px;
        background: #f0f4f8;
        box-shadow: none;
        border: #f0f4f8;
        display: inline-block;
        content: '\e80d';
        color: #818c9b;

        &:active {
          color: #096DD9;
        }
      }

      .doing {
        animation: rotating 2s linear infinite;
      }
    }

    // 表格
    .table-container {
      max-height: calc(100vh - 315px);
      background: #fff;
      box-sizing: border-box;
      padding: 20px;
      margin-bottom: 20px;

      ::v-deep(.el-scrollbar__wrap) {
        height: calc(100vh - 355px)!important;
      }

      .table-content {
        overflow-y: auto;
        border-radius: 4px 4px 0 0;
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
              cursor: pointer;
            }

            .retry {
              margin-right: 20px;
            }
          }
        }
      }

      // 显示更多
      .load-more {
        text-align: center;
      }
    }
  }

  // 查看paload
  ::v-deep(.el-dialog) {
    .el-dialog__header {
      border-bottom: 1px solid #e6ebf2;
      margin-bottom: 25px;
      padding: 20px 25px;
    }

    .el-dialog__header {
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

    .el-dialog__title {
      font-size: 16px;
      color: #082340;
      font-weight: 500;
    }

    .el-dialog__body {
      border: none;
      padding: 0px 20px 25px 20px;
    }

    // tab切换
    .tab-container {
      display: flex;

      div {
        width: 120px;
        height: 40px;
        text-align: center;
        line-height: 40px;
        background: #e6ebf2;
        margin-right: 5px;
        font-size: 14px;
        color: #082340;
        cursor: pointer;
        border-radius: 4px 4px 0 0;
      }

      .active {
        color: #fff;
        background: #042749;
      }
    }

    // payload/trigger
    .payload-content,
    .trigger-content {
      height: 500px;
    }

    .payload-content {
      padding: 20px;
      border: 1px solid #e6ebf2;
    }

    // 触发器
    .trigger-content {
      overflow-y: auto;
      //padding: 20px;
      border: 1px solid #e6ebf2;

      .trigger-title {
        margin-bottom: 10px;
      }

      .trigger-table,
      .verify-table {
        border: 1px solid #eceef6;
        border-bottom: 0;

        th {
          text-align: center;
        }

        td {
          border-right: 1px solid #eceef6;
        }

        td:last-of-type {
          border-right: 0;
        }

        td:nth-of-type(2) {
          text-align: center;
        }

        .el-table__row:hover > td {
          background-color: #ffffff !important;
        }

        .el-table__row--striped:hover > td {
          background-color: #fafafa !important;
        }
      }

      .trigger-table {

        td:first-of-type,
        td:last-of-type {
          .cell {
            padding-left: 20px;
          }
        }

        .hide-secret,
        .display-secret {
          display: flex;
          align-items: center;
          justify-content: center;
          width: 20px;
          height: 20px;
          border-radius: 4px;
          cursor: pointer;
          //margin-left: 10px;
        }

        .hide-container {
          display: flex;
          align-items: center;

          span {
            margin-right: 5px;
          }

          .hide-secret {
            position: relative;
            top: -2px;

            .jm-icon-input-visible::before {
              content: '\e803';
            }

            &:hover {
              background: #eff7ff;
            }
          }
        }

        .display-container {
          display: flex;
          align-items: center;
          //justify-content: space-between;
          .param-value {
            flex: 1;
            margin-right: 5px;
          }

          .trigger-params-value {
            width: 280px;
          }

          .display-secret {
            .jm-icon-input-invisible::before {
              content: '\e800';
            }

            &:hover {
              background: #eff7ff;
            }
          }
        }
      }

      // 认证
      .verify-title {
        margin: 25px 0 10px 0;
      }

      .verify-table {
        td {
          text-align: center;
        }
      }

      // 匹配认证
      .matching-title {
        margin: 25px 0 10px 0;
      }

      .matching-container {
        min-height: 40px;
        border-radius: 4px;
        background: #f5f8fb;
        font-size: 16px;
        line-height: 20px;
        padding: 17px 20px;
        word-wrap: break-word;
        box-sizing: border-box;
      }

      // 为空时
      .matching-null {
        font-size: 14px;
        color: #909399;
      }
    }
  }
}
</style>
