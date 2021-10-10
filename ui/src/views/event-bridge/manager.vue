<template>
  <router-view v-if="childRoute"/>
  <div v-else class="event-bridge-manager" v-loading="loading">
    <div class="right-top-btn">
      <router-link :to="{name: 'index'}">
        <jm-button type="primary" class="jm-icon-button-cancel" size="small">关闭</jm-button>
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
    <div class="content">
      <div class="item" v-for="eb of eventBridges" :key="eb.id">
        <div class="wrapper">
          <router-link :to="{name: 'event-bridge-detail', params: { id: eb.id }}">
            <div class="name ellipsis">{{ eb.name }}</div>
          </router-link>
          <div class="time">最后修改时间：{{ datetimeFormatter(eb.lastModifiedTime) }}</div>
        </div>
        <div class="operation">
          <button :class="{del: true, doing: deletings[eb.id]}" @click="del(eb.id)"></button>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, getCurrentInstance, onBeforeMount, ref, Ref } from 'vue';
import {
  onBeforeRouteUpdate,
  RouteLocationNormalized,
  RouteLocationNormalizedLoaded,
  useRoute,
  useRouter,
} from 'vue-router';
import { START_PAGE_NUM } from '@/utils/constants';
import { datetimeFormatter } from '@/utils/formatter';
import { IQueryForm } from '@/model/modules/event-bridge';
import { queryEventBridge } from '@/api/view-no-auth';
import { deleteEventBridge, saveEventBridge } from '@/api/event-bridge';
import { IEventBridgeVo } from '@/api/dto/event-bridge';
import { EventBridgeSourceTypeEnum } from '@/api/dto/enumeration';

function changeView(childRoute: Ref<boolean>, route: RouteLocationNormalizedLoaded | RouteLocationNormalized) {
  childRoute.value = route.matched.length > 2;
}

export default defineComponent({
  setup() {
    const initQueryForm: IQueryForm = {
      pageNum: START_PAGE_NUM,
      // 一次性获取10w条，达到获取所有目的
      pageSize: 100 * 1000,
    };

    const { proxy } = getCurrentInstance() as any;
    const router = useRouter();
    const queryForm = ref<IQueryForm>({ ...initQueryForm });
    const eventBridges = ref<IEventBridgeVo[]>([]);
    const loading = ref<boolean>(false);
    const deletings = ref<{ [name: string]: boolean }>({});

    const loadEventBridge = async (reset?: boolean) => {
      if (reset) {
        queryForm.value = { ...initQueryForm };
      }

      try {
        loading.value = true;
        const { list } = await queryEventBridge({ ...queryForm.value });
        eventBridges.value = list;
      } catch (err) {
        proxy.$throw(err, proxy);
      } finally {
        loading.value = false;
      }
    };

    // 初始化事件桥接器列表
    onBeforeMount(() => loadEventBridge());

    const childRoute = ref<boolean>(false);
    changeView(childRoute, useRoute());
    onBeforeRouteUpdate(to => changeView(childRoute, to));

    return {
      childRoute,
      loading,
      deletings,
      datetimeFormatter,
      eventBridges,
      create: async () => {
        const { bridge: { id } } = await saveEventBridge({
          bridge: {
            name: 'Webhook',
          },
          source: {
            name: '',
            type: EventBridgeSourceTypeEnum.WEBHOOK,
          },
          targets: [{
            // ref: '',
            name: '',
            transformers: [],
          }],
        });
        await router.push({ name: 'event-bridge-detail', params: { id } });
      },
      del: (id: string) => {
        if (deletings.value[id]) {
          return;
        }

        proxy.$confirm('确定要删除吗?', '删除事件桥接器', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning',
        }).then(() => {
          deletings.value[id] = true;

          deleteEventBridge(id).then(() => {
            proxy.$success('删除成功');

            const index = eventBridges.value.findIndex(eb => eb.id === id);
            eventBridges.value.splice(index, 1);
          }).catch((err: Error) => {
            proxy.$throw(err, proxy);
          }).finally(() => {
            delete deletings.value[id];
          });
        }).catch(() => {
        });
      },
    };
  },
});
</script>

<style scoped lang="less">
.event-bridge-manager {
  padding: 15px;
  background-color: #FFFFFF;

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
        color: #B5BDC6;
      }

      &.add {
        margin: 0.5%;
        width: 19%;
        min-width: 260px;
        height: 120px;
        background-color: #FFFFFF;
        border: 1px dashed #B5BDC6;
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
      background-color: #FFFFFF;
      box-shadow: 0 0 8px 0 #9EB1C5;

      &:hover {
        box-shadow: 0 0 12px 0 #9EB1C5;

        .operation {
          display: block;
        }
      }

      .wrapper {
        padding: 15px;
        border: 1px solid transparent;
        height: 88px;

        &:hover {
          border-color: #096DD9;
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
        color: #6B7B8D;
      }

      .operation {
        display: none;
        position: absolute;
        right: 10px;
        top: 10px;

        button {
          width: 30px;
          height: 30px;
          background-color: #FFFFFF;
          border: 0;
          background-position: center center;
          background-repeat: no-repeat;
          cursor: pointer;

          &:active {
            background-color: #EFF7FF;
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
}
</style>