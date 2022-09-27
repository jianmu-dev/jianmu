<template>
  <jm-scrollbar>
    <div
      class="node-library-manager"
      v-scroll="{
        loadMore: btnDown,
        scrollableEl,
      }"
    >
      <div class="right-top-btn">
        <router-link :to="{ name: 'index' }">
          <jm-button type="primary" class="jm-icon-button-cancel" size="small"
          >关闭
          </jm-button
          >
        </router-link>
      </div>
      <div class="menu-bar">
        <button class="add" @click="creationActivated = true">
          <div class="label">新增本地节点</div>
        </button>
      </div>
      <div class="title">
        <span>建木节点库</span>
        <span class="desc">（共有 {{ total }} 个节点定义）</span>
      </div>

      <div v-loading="firstLoading" class="content">
        <jm-empty v-if="nodeLibraryListData.length === 0"/>
        <div
          v-else
          v-for="(i, idx) in nodeLibraryListData"
          :key="idx"
          class="item"
        >
          <div class="deprecated" v-if="i.deprecated">
            <jm-tooltip placement="top-start">
              <template #content>
                <div style="line-height: 20px">
                  由于某些原因，该节点不被推荐使用（如该节点可<br/>能会导致一些已知问题或有更好的节点可替代它）
                </div>
              </template>
              <img src="~@/assets/svgs/node-library/deprecated.svg" alt="">
            </jm-tooltip>
          </div>
          <div class="item-t">
            <span
              class="item-t-t"
              v-if="i.ownerType === OwnerTypeEnum.LOCAL"
            >
              <jm-text-viewer :value="i.name"/>
            </span
            >
            <a
              v-else
              target="_blank"
              class="item-t-t"
              :href="`https://jianmuhub.com/${i.ownerRef}/${i.ref}`"
            >
              <jm-text-viewer :value="i.name"/>
            </a
            >
            <p class="item-t-mid">
              <jm-text-viewer :value="`${i.ownerName} / ${i.ref}`"/>
            </p>
            <p class="item-t-btm">
              <jm-text-viewer :value="`${i.description || '无'}`"/>
            </p>
          </div>
          <div
            class="item-mid"
            :class="{ 'is-background': !i.isDirectionDown }"
          >
            <i
              @click="clickVersion(i)"
              class="down"
              :class="{ 'direction-down': i.isDirectionDown }"
            ></i>
            <jm-scrollbar max-height="75px">
              <div
                class="item-mid-items"
                :class="{ 'is-scroll': i.isDirectionDown }"
              >
                <div
                  v-for="(version, versionIdx) in i.versions"
                  :key="versionIdx"
                  class="item-mid-item"
                >
                  <span v-if="i.ownerType === OwnerTypeEnum.LOCAL">
                   {{ version }}
                  </span>
                  <a
                    v-else
                    target="_blank"
                    :href="`https://jianmuhub.com/${i.ownerRef}/${i.ref}/${version}`"
                  >
                    {{ version }}
                  </a
                  >
                </div>
              </div>
            </jm-scrollbar>
          </div>
          <div v-show="!i.isDirectionDown" class="item-btm">
            <div>
              <jm-tooltip
                v-if="i.ownerType !== OwnerTypeEnum.LOCAL"
                content="同步"
                placement="top"
              >
                <button
                  @click="syncNode(i)"
                  @keypress.enter.prevent
                  class="sync"
                  :class="{ doing: i.isSync }"
                ></button>
              </jm-tooltip>
              <jm-tooltip content="删除" placement="top">
                <button
                  @click="deleteNode(i)"
                  @keypress.enter.prevent
                  class="del"
                  :class="{ doing: i.isDel }"
                ></button>
              </jm-tooltip>
            </div>
            <div class="item-btm-r">
              <jm-text-viewer :value="`by ${i.creatorName}`"/>
            </div>
          </div>
          <div
            class="item-pos"
            :class="{ 'node-definition-default-icon': !i.icon, 'deprecated-icon':i.deprecated}"
          >
            <img
              v-if="i.icon"
              :src="`${i.icon}?imageMogr2/thumbnail/81x/sharpen/1`"
            />
          </div>
        </div>
      </div>

      <!-- 显示更多 -->
      <div class="load-more">
        <jm-load-more :state="loadState" :load-more="btnDown"></jm-load-more>
      </div>
      <node-editor
        v-if="creationActivated"
        @closed="creationActivated = false"
        @completed="handleCreation"
      />
    </div>
  </jm-scrollbar>
</template>

<script lang="ts">
import {
  defineComponent,
  getCurrentInstance,
  reactive,
  ref,
  Ref,
  inject,
} from 'vue';
import { fetchNodeLibrary, fetchNodeLibraryList } from '@/api/view-no-auth';
import { deleteNodeLibrary, syncNodeLibrary } from '@/api/node-library';
import { INode } from '@/model/modules/node-library';
import NodeEditor from './node-editor.vue';
import { OwnerTypeEnum } from '@/api/dto/enumeration';
import { StateEnum } from '@/components/load-more/enumeration';
import { Mutable } from '@/utils/lib';
import { START_PAGE_NUM } from '@/utils/constants';

export default defineComponent({
  components: {
    NodeEditor,
  },
  setup() {
    const { proxy } = getCurrentInstance() as any;
    const nodeLibraryListParameter = reactive<{
      pageNum: number;
      pageSize: number;
    }>({
      pageNum: START_PAGE_NUM,
      pageSize: 12,
    });
    const nodeLibraryListData = reactive<Mutable<INode>[]>([]);
    const firstLoading = ref<boolean>(true);
    const total = ref<number>(0);
    const creationActivated = ref<boolean>(false);
    const scrollableEl = inject('scrollableEl');
    // 显示更多
    const loadState = ref<StateEnum>(StateEnum.NONE);
    // 总页数
    const pages = ref<number>(0);
    // 获取数据
    const nodeListData = (
      nodeLibraryListData: INode[],
      nodeLibraryListParameter: { pageNum: number; pageSize: number },
      loading: Ref<boolean>,
      total: Ref<number>,
    ) => {
      fetchNodeLibraryList(nodeLibraryListParameter)
        .then(res => {
          pages.value = res.pages;
          total.value = res.total;
          loading.value = false;
          nodeLibraryListData.push(...res.list);
          if (pages.value > nodeLibraryListParameter.pageNum) {
            loadState.value = StateEnum.MORE;
          }
          if (pages.value === nodeLibraryListParameter.pageNum) {
            loadState.value = StateEnum.NO_MORE;
          }
          if (total.value === 0) {
            loadState.value = StateEnum.NONE;
          }
        })
        .catch((err: Error) => {
          proxy.$throw(err, proxy);
        });
    };
    nodeListData(
      nodeLibraryListData,
      nodeLibraryListParameter,
      firstLoading,
      total,
    );

    // 加载更多
    const loadMore = () => {
      const bottomLoading = ref<boolean>(false);
      const btnDown = () => {
        if (nodeLibraryListParameter.pageNum < pages.value) {
          loadState.value = StateEnum.LOADING;
          nodeLibraryListParameter.pageNum++;
          bottomLoading.value = true;
          nodeListData(
            nodeLibraryListData,
            nodeLibraryListParameter,
            bottomLoading,
            total,
          );
        }
      };
      return {
        btnDown,
      };
    };

    // 删除某一项
    const deleteNodeLibraryListData = (i: INode) => {
      const idx = nodeLibraryListData.indexOf(i);
      nodeLibraryListData.splice(idx, 1);
      total.value -= 1;
    };

    // 删除
    const deleteNode = (i: INode) => {
      if (i.isDel) {
        return;
      }

      let msg = '<div>确定要删除节点吗?</div>';
      msg += `<div style="margin-top: 5px; font-size: 12px; line-height: normal;">名称：${i.name}</div>`;

      proxy
        .$confirm(msg, '删除节点', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning',
          dangerouslyUseHTMLString: true,
        })
        .then(() => {
          i.isDel = true;
          deleteNodeLibrary(i.ownerRef, i.ref)
            .then(() => {
              proxy.$success('删除成功');
              deleteNodeLibraryListData(i);
            })
            .catch((err: Error) => {
              proxy.$throw(err, proxy);
            })
            .finally(() => {
              i.isDel = false;
            });
        });
    };

    // 点击版本
    const clickVersion = (i: INode) => {
      i.isDirectionDown = !i.isDirectionDown;
    };

    // 同步
    const syncNode = (i: INode) => {
      proxy
        .$confirm('确定要同步吗?', '同步DSL', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'info',
        })
        .then(() => {
          i.isSync = true;
          syncNodeLibrary(i.ownerRef, i.ref)
            .then(() => {
              proxy.$success('同步成功');
            })
            .catch((err: Error) => {
              proxy.$throw(err, proxy);
            })
            .finally(async () => {
              i.isSync = false;
              // 点击同步按钮后，重新获取该节点的信息。
              try {
                const { deprecated } = await fetchNodeLibrary(i.ownerRef, i.ref);
                nodeLibraryListData.forEach(item => {
                  if (item.ref === i.ref) {
                    item.deprecated = deprecated;
                  }
                });
              } catch (err) {
                proxy.$throw(err, proxy);
              }
            });
        });
    };
    return {
      loadState,
      scrollableEl,
      clickVersion,
      ...loadMore(),
      deleteNode,
      firstLoading,
      nodeLibraryListData,
      syncNode,
      total,
      creationActivated,
      handleCreation: () => {
        nodeLibraryListData.length = 0;
        nodeLibraryListParameter.pageNum = 1;
        nodeLibraryListParameter.pageSize = 12;
        firstLoading.value = true;
        total.value = 0;
        nodeListData(
          nodeLibraryListData,
          nodeLibraryListParameter,
          firstLoading,
          total,
        );
      },
      OwnerTypeEnum,
    };
  },
});
</script>

<style scoped lang="less">
.node-library-manager {
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

  .menu-bar {
    button {
      position: relative;

      .label {
        position: absolute;
        left: 0;
        bottom: 45px;
        width: 100%;
        text-align: center;
        font-size: 18px;
        color: #b5bdc6;
      }

      &.add {
        margin: 0.5%;
        width: 24%;
        min-width: 230px;
        height: 200px;
        background-color: #ffffff;
        border: 1px dashed #b5bdc6;
        background-image: url('@/assets/svgs/btn/add.svg');
        background-position: center 55px;
        background-repeat: no-repeat;
        cursor: pointer;
      }
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
    min-height: 200px;

    .item {
      margin: 0.5%;
      width: 24%;
      min-width: 230px;
      background-color: #ffffff;
      box-shadow: 0 0 12px 4px #edf1f8;
      padding: 15px;
      position: relative;
      box-sizing: border-box;

      .deprecated {
        position: absolute;
        top: 0;
        right: 0;

        img {
          width: 45px;
          height: 45px;
        }
      }

      .item-t {
        display: flex;
        flex-direction: column;
        color: #082340;
        max-width: 75%;

        > p {
          margin-bottom: 10px;
        }

        .item-t-t {
          color: #082340;
          text-decoration: none;
          margin-bottom: 10px;
          font-size: 16px;
        }

        a.item-t-t:hover {
          color: #096dd9;
        }

        .item-t-mid {
          font-size: 14px;
        }

        .item-t-btm {
          font-size: 14px;
          color: #385775;
        }
      }

      .item-mid {
        background: #f8fcff;
        margin: 0px -15px 0px -15px;
        padding: 6px 0px 6px 15px;
        position: relative;
        height: 62px;

        .down {
          width: 16px;
          height: 16px;
          position: absolute;
          right: 20px;
          top: 10px;
          cursor: pointer;
          z-index: 2;
          background-image: url('@/assets/svgs/nav/top/down.svg');
        }

        .down.direction-down {
          transform: rotate(180deg);
        }

        .item-mid-items {
          display: flex;
          justify-content: flex-start;
          flex-wrap: wrap;
          padding-right: 50px;
          height: 26px;
          overflow: hidden;

          .item-mid-item {
            padding: 5px;
            font-size: 12px;
            color: #385775;
            background: #eff4f8;
            border-radius: 2px;
            margin-right: 10px;
            overflow-y: auto;
            margin-bottom: 10px;
            max-width: 350px;
          }
        }

        .item-mid-items.is-scroll {
          height: auto;
          overflow: auto;
        }
      }

      .item-mid.is-background {
        background: #ffffff;
        min-height: auto;
        overflow: hidden;
        height: 26px;
        margin-bottom: 10px;
      }

      .item-btm {
        display: flex;
        justify-content: space-between;

        & > div {
          width: 49%;
          display: flex;
          align-items: end;
        }

        button {
          width: 26px;
          height: 26px;
          background-color: transparent;
          border: 0;
          background-position: center center;
          background-repeat: no-repeat;
          margin-right: 16px;
          cursor: pointer;

          &:active {
            background-color: #eff7ff;
            border-radius: 4px;
          }
        }

        .sync {
          background-image: url('@/assets/svgs/btn/sync.svg');

          &.doing {
            animation: rotating 2s linear infinite;

            &:active {
              background-color: transparent;
            }
          }
        }

        .doing {
          opacity: 0.5;
          cursor: not-allowed;
        }

        .del {
          background-image: url('@/assets/svgs/btn/del.svg');
        }

        .item-btm-r {
          justify-content: end;
          color: #7c91a5;
          font-size: 14px;

          ::v-deep(.jm-text-viewer) {
            width: 100%;

            .content {
              .text-line {
                &:last-child {
                  text-align: right;

                  &::after {
                    display: none;
                  }
                }
              }
            }
          }
        }
      }

      .item-pos {
        position: absolute;
        top: 20px;
        right: 20px;
        width: 54px;
        height: 54px;
        border-radius: 25.5%;
        overflow: hidden;

        &.deprecated-icon {
          opacity: .4;
        }

        img {
          width: 100%;
          height: 100%;
        }
      }

      .item-pos.node-definition-default-icon {
        background-image: url('@/assets/svgs/node-library/node-definition-default-icon.svg');
        background-size: 100%;
      }
    }
  }

  .load-more {
    text-align: center;
  }
}
</style>
