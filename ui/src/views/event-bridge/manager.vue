<template>
  <router-view v-if="childRoute" />
  <div v-else class="event-bridge-manager" v-scroll.current="loadMore">
    <div class="right-top-btn">
      <router-link :to="{ name: 'index' }">
        <jm-button type="primary" class="jm-icon-button-cancel" size="small"
          >关闭</jm-button
        >
      </router-link>
    </div>
    <div class="menu-bar">
      <button class="add" @click="create">
        <div class="label">新增事件桥接器</div>
      </button>
    </div>
    <div class="title">
      <span>事件桥接器</span>
      <span class="desc">（共有 {{ eventBridges.length }} 个事件桥接器）</span>
    </div>
    <div class="content" v-loading="loading">
      <jm-empty v-if="eventBridges.length === 0" />
      <div v-else class="item" v-for="eb of eventBridges" :key="eb.id">
        <div class="wrapper">
          <router-link
            :to="{ name: 'event-bridge-detail', params: { id: eb.id } }"
          >
            <div class="name ellipsis">{{ eb.name }}</div>
          </router-link>
          <div class="time">
            最后修改时间：{{ datetimeFormatter(eb.lastModifiedTime) }}
          </div>
        </div>
        <div class="operation">
          <button
            :class="{ del: true, doing: deletings[eb.id] }"
            @click="del(eb.id)"
          ></button>
        </div>
      </div>
    </div>
    <div v-if="isShowMore" @click="loadMore" class="bottom">
      <span>显示更多</span>
      <i class="btm-down" :class="{ 'btn-loading': moreLoading }"></i>
    </div>
  </div>
</template>

<script lang="ts">
import {
  defineComponent,
  getCurrentInstance,
  onBeforeMount,
  ref,
  Ref,
} from 'vue';
import {
  onBeforeRouteUpdate,
  RouteLocationNormalized,
  RouteLocationNormalizedLoaded,
  useRoute,
  useRouter,
} from 'vue-router';
import { START_PAGE_NUM } from '@/utils/constants';
import { datetimeFormatter } from '@/utils/formatter';
import { queryEventBridge } from '@/api/view-no-auth';
import { deleteEventBridge, saveEventBridge } from '@/api/event-bridge';
import { IEventBridgeVo } from '@/api/dto/event-bridge';
import { EventBridgeSourceTypeEnum } from '@/api/dto/enumeration';
import { IPageVo } from '@/api/dto/common';

function changeView(
  childRoute: Ref<boolean>,
  route: RouteLocationNormalizedLoaded | RouteLocationNormalized,
) {
  childRoute.value = route.matched.length > 2;
}

export default defineComponent({
  setup() {
    const count = ref<number>(START_PAGE_NUM);
    const isShowMore = ref<boolean>(false);
    const { proxy } = getCurrentInstance() as any;
    const router = useRouter();
    const eventBridges = ref<IEventBridgeVo[]>([]);
    const eventBridgesResult = ref<IPageVo<IEventBridgeVo>>();
    const loading = ref<boolean>(false);
    const moreLoading = ref<boolean>(false);
    const deletings = ref<{ [name: string]: boolean }>({});
    const loadEventBridge = async (page?: boolean) => {
      const l = page ? moreLoading : loading;
      page ? (count.value += 1) : count.value;
      try {
        l.value = true;
        eventBridgesResult.value = await queryEventBridge({
          pageNum: count.value,
          pageSize: 10,
        });
        eventBridges.value.push(...eventBridgesResult.value.list);
        if (count.value >= eventBridgesResult.value.pages) {
          isShowMore.value = false;
        } else {
          isShowMore.value = true;
        }
      } catch (err) {
        proxy.$throw(err, proxy);
      } finally {
        l.value = false;
      }
    };
    const loadMore = async () => {
      if (
        !eventBridgesResult.value ||
        count.value >= eventBridgesResult.value.pages
      ) {
        return;
      }
      await loadEventBridge(true);
    };
    // 初始化事件桥接器列表
    onBeforeMount(() => loadEventBridge());
    const childRoute = ref<boolean>(false);
    changeView(childRoute, useRoute());
    onBeforeRouteUpdate(to => changeView(childRoute, to));

    return {
      isShowMore,
      moreLoading,
      childRoute,
      loading,
      deletings,
      datetimeFormatter,
      eventBridges,
      create: async () => {
        const {
          bridge: { id },
        } = await saveEventBridge({
          bridge: {
            name: 'Webhook',
          },
          source: {
            name: '',
            type: EventBridgeSourceTypeEnum.WEBHOOK,
          },
          targets: [
            {
              // ref: '',
              name: '',
              transformers: [],
            },
          ],
        });
        await router.push({ name: 'event-bridge-detail', params: { id } });
      },
      del: (id: string) => {
        if (deletings.value[id]) {
          return;
        }

        const { name } = eventBridges.value.find(
          item => item.id === id,
        ) as IEventBridgeVo;

        let msg = '<div>确定要删除事件桥接器吗?</div>';
        msg += `<div style="margin-top: 5px; font-size: 12px; line-height: normal;">名称：${name}</div>`;

        proxy
          .$confirm(msg, '删除事件桥接器', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning',
            dangerouslyUseHTMLString: true,
          })
          .then(() => {
            deletings.value[id] = true;

            deleteEventBridge(id)
              .then(() => {
                proxy.$success('删除成功');

                const index = eventBridges.value.findIndex(eb => eb.id === id);
                eventBridges.value.splice(index, 1);
              })
              .catch((err: Error) => {
                proxy.$throw(err, proxy);
              })
              .finally(() => {
                delete deletings.value[id];
              });
          })
          .catch(() => {});
      },
      loadMore,
    };
  },
});
</script>

<style scoped lang="less">
.event-bridge-manager {
  padding: 15px;
  background-color: #ffffff;
  min-height: 70vh;
  .right-top-btn {
    position: fixed;
    right: 20px;
    top: 78px;

    .jm-icon-button-cancel::before {
      font-weight: bold;
    }
  }

  .menu-bar {
    button {
      position: relative;

      .label {
        position: absolute;
        left: 0;
        bottom: 15px;
        width: 100%;
        text-align: center;
        font-size: 18px;
        color: #b5bdc6;
      }

      &.add {
        margin: 0.5%;
        width: 19%;
        min-width: 260px;
        height: 120px;
        background-color: #ffffff;
        border: 1px dashed #b5bdc6;
        background-image: url('@/assets/svgs/btn/add.svg');
        background-position: center 20px;
        background-repeat: no-repeat;
        cursor: pointer;
      }
    }
  }

  .title {
    margin-top: 30px;
    margin-left: 0.5%;
    margin-bottom: 20px;
    font-size: 18px;
    font-weight: bold;
    color: #082340;

    .desc {
      font-weight: normal;
      margin-left: 12px;
      font-size: 14px;
      color: #082340;
      opacity: 0.46;
    }
  }

  .content {
    display: flex;
    flex-wrap: wrap;

    .item {
      position: relative;
      margin: 0.5%;
      width: 19%;
      min-width: 260px;
      height: 120px;
      background-color: #ffffff;
      box-shadow: 0 0 8px 0 #9eb1c5;

      &:hover {
        box-shadow: 0 0 12px 0 #9eb1c5;

        .operation {
          display: block;
        }
      }

      .wrapper {
        padding: 15px;
        border: 1px solid transparent;
        height: 88px;

        &:hover {
          border-color: #096dd9;
        }

        a {
          text-decoration: none;
        }

        .name {
          font-size: 20px;
          font-weight: bold;
          color: #082340;

          &:hover {
            text-decoration: underline;
          }
        }

        .ellipsis {
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
        }
      }

      .time {
        position: absolute;
        left: 15px;
        bottom: 15px;
        font-size: 13px;
        color: #6b7b8d;
      }

      .operation {
        display: none;
        position: absolute;
        right: 6px;
        top: 8px;

        button {
          width: 22px;
          height: 22px;
          background-color: #ffffff;
          border: 0;
          background-position: center center;
          background-repeat: no-repeat;
          background-size: contain;
          cursor: pointer;

          &:active {
            background-color: #eff7ff;
            border-radius: 4px;
          }

          &.del {
            background-image: url('@/assets/svgs/btn/del.svg');
          }

          &.doing {
            opacity: 0.5;
            cursor: not-allowed;

            &:active {
              background-color: transparent;
            }
          }
        }
      }
    }
  }
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
</style>
