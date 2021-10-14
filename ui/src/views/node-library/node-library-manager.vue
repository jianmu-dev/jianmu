<template>
  <div class="node-library-manager">
    <div class="right-top-btn">
      <router-link :to="{name: 'index'}">
        <jm-button type="primary" class="jm-icon-button-cancel" size="small">关闭</jm-button>
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
      <div
        v-for="(i,idx) in nodeLibraryListData"
        :key="idx"
        class="item">
        <div class="item-t">
          <span class="item-t-t ellipsis" v-if="i.ownerType === OwnerTypeEnum.LOCAL">{{ i.name }}</span>
          <a v-else target="_blank" class="item-t-t ellipsis" :href="`https://hub.jianmu.dev/${i.ownerRef}/${i.ref}`">{{
              i.name
            }}</a>
          <p class="item-t-mid ellipsis">{{ i.ownerName }} / {{ i.ref }}</p>
          <p class="item-t-btm ellipsis">{{ i.description || '无' }}</p>
        </div>
        <div class="item-mid" :class="{'is-background':!i.isDirectionDown}">
          <i @click="clickVersion(i)" class="down" :class="{'direction-down': i.isDirectionDown}"></i>
          <jm-scrollbar max-height="75px">
            <div class="item-mid-items" :class="{'is-scroll':i.isDirectionDown}">
              <div
                v-for="(version,versionIdx) in i.versions"
                :key="versionIdx"
                class="item-mid-item ellipsis">
                <span v-if="i.ownerType === OwnerTypeEnum.LOCAL">{{ version }}</span>
                <a v-else target="_blank" :href="`https://hub.jianmu.dev/${i.ownerRef}/${i.ref}/${version}`">{{
                    version
                  }}</a>
              </div>
            </div>
          </jm-scrollbar>
        </div>
        <div v-show="!i.isDirectionDown" class="item-btm">
          <div>
            <jm-tooltip v-if="i.ownerType !== OwnerTypeEnum.LOCAL" content="同步" placement="top">
              <button @click="syncNode(i)" class="sync" :class="{doing:i.isSync}"></button>
            </jm-tooltip>
            <jm-tooltip content="删除" placement="top">
              <button @click="deleteNode(i)" class="del" :class="{doing:i.isDel}"></button>
            </jm-tooltip>
          </div>
          <div class="item-btm-r ellipsis">
            by {{ i.creatorName }}
          </div>
        </div>
        <div class="item-pos" :class="{'node-definition-default-icon':!i.icon}">
          <img v-if="i.icon" :src="`${i.icon}?imageView2/2/w/54/h/54/interlace/1/q/100`">
        </div>
      </div>
    </div>

    <div v-if="noMore && !firstLoading" @click="btnDown" class="bottom">
      <span>显示更多</span>
      <i class="btm-down" :class="{'btn-loading':bottomLoading}"></i>
    </div>
    <node-editor v-if="creationActivated" @closed="creationActivated = false" @completed="handleCreation"/>
  </div>
</template>

<script lang="ts">
import { defineComponent, getCurrentInstance, reactive, ref, Ref } from 'vue';
import { fetchNodeLibraryList } from '@/api/view-no-auth';
import { deleteNodeLibrary, syncNodeLibrary } from '@/api/node-library';
import { INode } from '@/model/modules/node-library';
import NodeEditor from './node-editor.vue';
import { OwnerTypeEnum } from '@/api/dto/enumeration';

export default defineComponent({
  components: {
    NodeEditor,
  },
  setup() {
    const { proxy } = getCurrentInstance() as any;
    const nodeLibraryListParameter = reactive<{ pageNum: number, pageSize: number }>({
      pageNum: 1,
      pageSize: 16,
    });
    const nodeLibraryListData = reactive<INode[]>([]);
    const firstLoading = ref<boolean>(true);
    const noMore = ref<boolean>(true);
    const total = ref<number>(0);
    const creationActivated = ref<boolean>(false);

    // 获取数据
    const nodeListData = (nodeLibraryListData: INode[], nodeLibraryListParameter: { pageNum: number, pageSize: number }, loading: Ref<boolean>, noMore: Ref<boolean>, total: Ref<number>) => {
      if (!noMore.value) return;
      fetchNodeLibraryList(nodeLibraryListParameter).then(res => {
        total.value = res.total;
        loading.value = false;
        if (res.list.length === 0) {
          noMore.value = false;
          return;
        }
        if (res.list.length === res.total) {
          noMore.value = false;
        }
        nodeLibraryListData.push(...res.list);
      }).catch((err: Error) => {
        proxy.$throw(err, proxy);
      });
    };

    nodeListData(nodeLibraryListData, nodeLibraryListParameter, firstLoading, noMore, total);

    // 加载更多
    const loadMore = () => {
      const bottomLoading = ref<boolean>(false);
      const btnDown = () => {
        if (bottomLoading.value) return;
        if (!noMore.value) return;
        nodeLibraryListParameter.pageNum++;
        bottomLoading.value = true;
        nodeListData(nodeLibraryListData, nodeLibraryListParameter, bottomLoading, noMore, total);
      };
      return {
        bottomLoading,
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
      proxy.$confirm('确定要删除节点吗?', '删除节点', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }).then(() => {
        i.isDel = true;
        deleteNodeLibrary(i.ownerRef, i.ref).then(() => {
          proxy.$success('删除成功');
          deleteNodeLibraryListData(i);
        }).catch((err: Error) => {
          proxy.$throw(err, proxy);
        }).finally(() => {
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
      proxy.$confirm('确定要同步吗?', '同步DSL', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info',
      }).then(() => {
        i.isSync = true;
        syncNodeLibrary(i.ownerRef, i.ref).then(() => {
          proxy.$success('同步成功');
        }).catch((err: Error) => {
          proxy.$throw(err, proxy);
        }).finally(() => {
          i.isSync = false;
        });
      });
    };
    return {
      clickVersion,
      ...loadMore(),
      deleteNode,
      firstLoading,
      nodeLibraryListData,
      noMore,
      syncNode,
      total,
      creationActivated,
      handleCreation: () => {
        nodeLibraryListData.length = 0;
        nodeLibraryListParameter.pageNum = 1;
        nodeLibraryListParameter.pageSize = 16;
        firstLoading.value = true;
        noMore.value = true;
        total.value = 0;
        nodeListData(nodeLibraryListData, nodeLibraryListParameter, firstLoading, noMore, total);
      },
      OwnerTypeEnum,
    };
  },
});
</script>

<style scoped lang="less">

.node-library-manager {
  padding: 16px 20px 25px 16px;
  background-color: #FFFFFF;

  .right-top-btn {
    position: fixed;
    right: 20px;
    top: 78px;

    .jm-icon-button-cancel::before {
      font-weight: bold;
    }
  }

  .ellipsis {
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
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
        color: #B5BDC6;
      }

      &.add {
        margin: 0.5%;
        width: 24%;
        min-width: 230px;
        height: 200px;
        background-color: #FFFFFF;
        border: 1px dashed #B5BDC6;
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
      background-color: #FFFFFF;
      box-shadow: 0 0 12px 4px #edf1f8;
      padding: 15px;
      position: relative;
      box-sizing: border-box;

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
          text-decoration: underline;
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
        background: #F8FCFF;
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
          transform: rotate(180deg)
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
            background: #EFF4F8;
            border-radius: 2px;
            margin-right: 10px;
            overflow-y: auto;
            margin-bottom: 10px;
            max-width: 350px;

            & > a {
              text-decoration: none;
              color: #385775;
            }

            & > a:hover {
              text-decoration: underline;
            }
          }
        }

        .item-mid-items.is-scroll {
          height: auto;
          overflow: auto;

        }
      }

      .item-mid.is-background {
        background: #FFFFFF;
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
          color: #7C91A5;
          font-size: 14px;
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

  .bottom {
    margin-top: 25px;
    color: #7B8C9C;
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