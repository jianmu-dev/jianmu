<template>
  <div class="worker-manager">
    <div class="right-top-btn">
      <router-link :to="{ name: 'index' }">
        <jm-button type="primary" class="jm-icon-button-cancel" size="small">关闭 </jm-button>
      </router-link>
    </div>
    <div class="title">
      <span>Worker列表</span>
      <span class="desc">（共有 {{ workerListData.length }} 个Worker）</span>
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
            <span class="id">ID: {{ i.id }}</span>
          </div>
          <div class="name">
            <span>名称：</span>
            <span>{{ i.name }}</span>
          </div>
          <div class="tags">
            <span>标签：</span>
            <span>{{ i.tags || '无' }}</span>
          </div>
          <div class="os">
            <span>操作系统：</span>
            <span>{{ i.os }}</span>
          </div>
          <div class="arch">
            <span>架构：</span>
            <span>{{ i.arch }}</span>
          </div>
          <div class="type">
            <span>类型：</span>
            <span>{{ i.type }}</span>
          </div>
          <div class="created-time">
            <span>注册时间：</span>
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

export default defineComponent({
  setup() {
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
      deletings,
      workerListData,
      datetimeFormatter,
      del: (id: string) => {
        if (deletings.value[id]) {
          return;
        }

        let msg = '<div>确定要删除Worker吗?</div>';
        msg += `<div style="margin-top: 5px; font-size: 12px; line-height: normal;">ID：${id}</div>`;
        proxy
          .$confirm(msg, '删除Worker', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning',
            dangerouslyUseHTMLString: true,
          })
          .then(() => {
            deletings.value[id] = true;
            deleteWorker(id)
              .then(() => {
                proxy.$success('删除成功');
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
      margin: 0.5%;
      width: 19%;
      min-width: 260px;
      height: 170px;
      background-color: #ffffff;
      box-shadow: 0px 0px 8px 4px #eff4f9;

      &:hover {
        box-shadow: 0px 6px 16px 4px #e6eef6;

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
        padding: 15px;
        border: 1px solid transparent;
        height: 138px;
        color: #6b7b8d;
        font-size: 13px;
        position: relative;

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
          margin-top: 5px;
          line-height: 20px;

          &:hover {
            color: #096dd9;
          }
        }

        // .tags {
        //     margin-top: 5px;
        //     line-height: 20px;
        // }

        // .os {
        //     margin-top: 5px;
        //     line-height: 20px;
        // }

        // .arch {
        //     margin-top: 5px;
        //     line-height: 20px;
        // }

        .created-time {
          position: absolute;
          bottom: 10px;
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
