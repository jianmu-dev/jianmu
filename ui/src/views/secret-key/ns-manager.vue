<template>
  <router-view v-if="childRoute"/>
  <div v-else class="secret-key-ns-manager">
    <div class="right-top-btn">
      <router-link :to="{name: 'index'}">
        <jm-button type="primary" class="jm-icon-button-cancel" size="small">关闭</jm-button>
      </router-link>
    </div>
    <div class="menu-bar">
      <button class="add" @click="add">
        <div class="label">新增命名空间</div>
      </button>
    </div>
    <div class="title">
      <span>命名空间</span>
      <span class="desc">（共有 {{ namespaces.length }} 个命名空间）</span>
    </div>
    <div class="content" v-loading="loading">
      <jm-empty v-if="namespaces.length === 0"/>
      <div v-else class="item" v-for="ns of namespaces" :key="ns.name">
        <div class="wrapper">
          <router-link :to="{name: 'manage-secret-key', params: { namespace: ns.name }}">
            <div class="name ellipsis">{{ ns.name }}</div>
          </router-link>
          <div class="description">
            <jm-scrollbar max-height="80px">
              <span v-html="(ns.description || '无').replace(/\n/g, '<br/>')"/>
            </jm-scrollbar>
          </div>
          <div class="time">最后修改时间：{{ datetimeFormatter(ns.lastModifiedTime) }}</div>
        </div>
        <div class="operation">
          <button :class="{del: true, doing: deletings[ns.name]}" @click="del(ns.name)"></button>
        </div>
      </div>
    </div>
    <ns-editor v-if="creationActivated" @closed="creationActivated = false" @completed="loadNamespace()"/>
  </div>
</template>

<script lang="ts">
import { defineComponent, getCurrentInstance, onBeforeMount, Ref, ref, toRefs } from 'vue';
import { createNamespacedHelpers, useStore } from 'vuex';
import { namespace } from '@/store/modules/secret-key';
import { IState } from '@/model/modules/secret-key';
import { onBeforeRouteUpdate, RouteLocationNormalized, RouteLocationNormalizedLoaded, useRoute } from 'vue-router';
import { deleteNamespace } from '@/api/secret-key';
import NsEditor from './ns-editor.vue';
import { datetimeFormatter } from '@/utils/formatter';

const { mapMutations, mapActions } = createNamespacedHelpers(namespace);

function changeView(childRoute: Ref<boolean>, route: RouteLocationNormalizedLoaded | RouteLocationNormalized) {
  childRoute.value = route.matched.length > 2;
}

export default defineComponent({
  components: {
    NsEditor,
  },
  setup() {
    const { proxy } = getCurrentInstance() as any;
    const state = useStore().state[namespace] as IState;
    const loading = ref<boolean>(false);
    const creationActivated = ref<boolean>(false);
    const deletings = ref<{ [name: string]: boolean }>({});

    const loadNamespace = async () => {
      try {
        loading.value = true;
        await proxy.listNamespace();
      } catch (err) {
        proxy.$throw(err, proxy);
      } finally {
        loading.value = false;
      }
    };

    // 初始化命名空间列表
    onBeforeMount(() => loadNamespace());

    const childRoute = ref<boolean>(false);
    changeView(childRoute, useRoute());
    onBeforeRouteUpdate(to => changeView(childRoute, to));

    return {
      ...toRefs(state),
      childRoute,
      loading,
      creationActivated,
      deletings,
      ...mapMutations({
        mutateNamespaceDeletion: 'mutateNamespaceDeletion',
      }),
      ...mapActions({
        listNamespace: 'listNamespace',
      }),
      datetimeFormatter,
      loadNamespace,
      add: () => {
        creationActivated.value = true;
      },
      del: (name: string) => {
        if (deletings.value[name]) {
          return;
        }

        let msg = '<div>确定要删除命名空间吗?</div>';
        msg += `<div style="margin-top: 5px; font-size: 12px; line-height: normal;">名称：${name}</div>`;

        proxy.$confirm(msg, '删除命名空间', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning',
          dangerouslyUseHTMLString: true,
        }).then(() => {
          deletings.value[name] = true;

          deleteNamespace(name).then(() => {
            proxy.$success('删除成功');

            delete deletings.value[name];

            // 同步命名空间列表（vuex状态）
            proxy.mutateNamespaceDeletion(name);
          }).catch((err: Error) => {
            proxy.$throw(err, proxy);

            delete deletings.value[name];
          });
        }).catch(() => {
        });
      },
    };
  },
});
</script>

<style scoped lang="less">
.secret-key-ns-manager {
  padding: 15px;
  background-color: #FFFFFF;
  margin-bottom: 25px;

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
        bottom: 40px;
        width: 100%;
        text-align: center;
        font-size: 18px;
        color: #B5BDC6;
      }

      &.add {
        margin: 0.5%;
        width: 19%;
        min-width: 260px;
        height: 170px;
        background-color: #FFFFFF;
        border: 1px dashed #B5BDC6;
        background-image: url('@/assets/svgs/btn/add.svg');
        background-position: center 45px;
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
      height: 170px;
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
        height: 138px;

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

        .description {
          margin-top: 6px;
          font-size: 13px;
          color: #6B7B8D;
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
        right: 6px;
        top: 8px;

        button {
          width: 22px;
          height: 22px;
          background-color: #FFFFFF;
          border: 0;
          background-position: center center;
          background-repeat: no-repeat;
          background-size: contain;
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
