<template>
  <div class="worker-manager">
    <div class="right-top-btn">
      <router-link :to="{ name: 'index' }">
        <jm-button type="primary" class="jm-icon-button-cancel" size="small">{{ t('workersManager.close') }}</jm-button>
      </router-link>
    </div>
    <div class="title">
      <span>{{ t('workersManager.title') }}</span>
      <span class="desc">{{ t('workersManager.total', { length: workerListData.length }) }}</span>
    </div>
    <div class="content">
      <jm-empty v-if="workerListData.length === 0" />
      <div v-else v-for="(i, idx) in workerListData" :key="idx" class="item">
        <div
          :class="{
            'state-bar': true,
            [i.status?.toLowerCase()]: true,
          }"
        ></div>
        <div class="wrapper">
          <div class="top">
            <span class="id">{{ i.id }}</span>
          </div>
          <div class="name">
            <span>{{ t('workersManager.name') }}</span>
            <span>{{ i.name }}</span>
          </div>
          <div class="tags">
            <span>{{ t('workersManager.label') }}</span>
            <span>{{ i.tags || t('workersManager.none') }}</span>
          </div>
          <div class="os">
            <span>{{ t('workersManager.os') }}</span>
            <span>{{ i.os }}</span>
          </div>
          <div class="arch">
            <span>{{ t('workersManager.arch') }}</span>
            <span>{{ i.arch }}</span>
          </div>
          <div class="type">
            <span>{{ t('workersManager.type') }}</span>
            <span>{{ i.type }}</span>
          </div>
          <div class="created-time">
            <span>{{ t('workersManager.time') }}</span>
            <span>{{ datetimeFormatter(i.createdTime) }}</span>
          </div>
        </div>
        <div class="operation">
          <button :class="{ del: true, doing: deletings[i.id] }" @click="del(i.id)" @keypress.enter.prevent></button>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, getCurrentInstance, onMounted, ref } from 'vue';
import { Mutable } from '@/utils/lib';
import { IWorkerVo } from '@/api/dto/worker';
import { fetchWorkerList, deleteWorker } from '@/api/worker';
import { datetimeFormatter } from '@/utils/formatter';
import { useLocale } from '@/utils/i18n';

export default defineComponent({
  setup() {
    const { t } = useLocale();
    const { proxy } = getCurrentInstance() as any;
    const deletings = ref<{ [name: string]: boolean }>({});

    const workerListData = ref<Mutable<IWorkerVo>[]>([]);
    const fetchWorkers = async () => {
      workerListData.value = await fetchWorkerList();
    };

    onMounted(async () => {
      await fetchWorkers();
    });
    return {
      t,
      deletings,
      workerListData,
      datetimeFormatter,
      del: (id: string) => {
        if (deletings.value[id]) {
          return;
        }

        let msg = `<div>${t('workersManager.confirmDeleteMsg')}</div>`;
        msg += `<div style="margin-top: 5px; font-size: 12px; line-height: normal;">ID：${id}</div>`;
        proxy
          .$confirm(msg, t('workersManager.confirmDeleteTitle'), {
            confirmButtonText: t('workersManager.confirm'),
            cancelButtonText: t('workersManager.cancel'),
            type: 'warning',
            dangerouslyUseHTMLString: true,
          })
          .then(() => {
            deletings.value[id] = true;
            deleteWorker(id)
              .then(() => {
                proxy.$success(t('workersManager.success'));
                delete deletings.value[id];
                const index = workerListData.value.findIndex(item => item.id === id);
                workerListData.value.splice(index, 1);
              })
              .catch((err: Error) => {
                proxy.$throw(err, proxy);
                delete deletings.value[id];
              });
          })
          .catch(() => {
            // 用户取消了操作，无需进一步处理
          });
      },
    };
  },
});
</script>

<style scoped lang="less">
.worker-manager {
  padding: 16px 20px 25px 16px;
  background-color: #ffffff;

  // height: calc(100vh - 185px);
  .right-top-btn {
    position: fixed;
    right: 20px;
    top: 78px;

    .jm-icon-button-cancel::before {
      font-weight: bold;
    }
  }

  .title {
    font-size: 20px;
    font-weight: bold;
    color: #082340;
    position: relative;
    padding-left: 20px;
    padding-right: 20px;
    margin: 30px -20px 20px;

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
      box-sizing: border-box;
      margin: 0.8% 0.8% 0 0;
      width: 19.2%;
      min-width: 260px;
      background-color: #ffffff;
      min-height: 166px;
      border-radius: 0px 0px 4px 4px;

      &:hover {
        box-shadow: 0px 0px 12px 4px #edf1f8;

        .wrapper {
          border: 1px solid transparent;
          border-top: none;
        }

        .operation {
          display: block;
        }
      }

      .state-bar {
        height: 6px;
        overflow: hidden;

        &.online {
          background-color: #3ebb03;
        }

        &.offline {
          background-color: #cf1524;
        }
      }

      .wrapper {
        min-height: 116px;
        position: relative;
        padding: 16px 20px 10px 20px;
        border: 1px solid #dee4eb;
        border-top: none;
        border-radius: 0px 0px 4px 4px;

        &:hover {
          border-color: #096dd9;
        }

        .top {
          display: flex;
          justify-content: space-between;
          align-items: center;
          white-space: nowrap;

          .id {
            color: #082340;
            font-size: 20px;
            font-weight: 500;

            &:hover {
              color: #096dd9;
            }
          }
        }

        .name {
          margin: 6px 0;
          color: #6b7b8d;
          font-size: 14px;
        }

        .tags {
          margin: 6px 0;
          color: #6b7b8d;
          font-size: 14px;
        }

        .os {
          margin: 6px 0;
          color: #6b7b8d;
          font-size: 14px;
        }

        .arch {
          margin: 6px 0;
          color: #6b7b8d;
          font-size: 14px;
        }

        .type {
          margin: 6px 0;
          color: #6b7b8d;
          font-size: 14px;
        }

        .created-time {
          margin: 6px 0;
          color: #6b7b8d;
          font-size: 14px;
        }
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
}
</style>
