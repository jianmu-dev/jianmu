<template>
  <router-view v-if="childRoute"/>
  <div v-else class="secret-key-ns-manager">
    <div class="right-top-btn">
      <router-link :to="{ name: 'index' }">
        <jm-button type="primary" class="jm-icon-button-cancel" size="small"
        >关闭
        </jm-button
        >
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
      <div v-else class="item">
        <!-- local-item -->
        <template v-if="credentialManagerType === CredentialManagerTypeEnum.LOCAL">
          <div class="local-item" v-for="ns of namespaces" :key="ns.name">
            <div class="wrapper">
              <router-link
                :to="{ name: 'manage-secret-key', params: { namespace: ns.name } }"
              >
                <div class="name">
                  <jm-text-viewer :value="ns.name"/>
                </div>
              </router-link>
              <div class="description">
                <jm-text-viewer :value="(ns.description || '无')" class="text-viewer"/>
              </div>
              <div class="time">
                最后修改时间：{{ datetimeFormatter(ns.lastModifiedTime) }}
              </div>
            </div>
            <div class="operation">
              <button
                :class="{ del: true, doing: deletings[ns.name] }"
                @click="del(ns.name)"
                @keypress.enter.prevent
              ></button>
            </div>
          </div>
        </template>
        <!-- vault-item-->
        <template v-else>
          <div class="vault-item" v-for="ns of namespaces" :key="ns.name">
            <div class="wrapper">
              <router-link :to="{name:'manage-secret-key',params:{namespace:ns.name}}">
                <div class="vault-icon"></div>
              </router-link>
              <router-link :to="{name:'manage-secret-key',params:{namespace:ns.name}}">
                <div class="vault-name">
                  <jm-text-viewer :value="ns.name"/>
                </div>
              </router-link>
            </div>
            <div class="operation">
              <button
                :class="{ del: true, doing: deletings[ns.name] }"
                @click="del(ns.name)"
              ></button>
            </div>
          </div>
        </template>
      </div>
    </div>
    <ns-editor
      v-if="creationActivated"
      :credential-manager-type="credentialManagerType"
      @closed="creationActivated = false"
      @completed="loadNamespace()"
    />
  </div>
</template>

<script lang="ts">
import {
  defineComponent,
  getCurrentInstance,
  onBeforeMount,
  Ref,
  ref,
  toRefs,
} from 'vue';
import { createNamespacedHelpers, useStore } from 'vuex';
import { namespace } from '@/store/modules/secret-key';
import { IState } from '@/model/modules/secret-key';
import {
  onBeforeRouteUpdate,
  RouteLocationNormalized,
  RouteLocationNormalizedLoaded,
  useRoute,
} from 'vue-router';
import { deleteNamespace } from '@/api/secret-key';
import NsEditor from './ns-editor.vue';
import { datetimeFormatter } from '@/utils/formatter';
import { CredentialManagerTypeEnum } from '@/api/dto/enumeration';

const { mapMutations, mapActions } = createNamespacedHelpers(namespace);

function changeView(
  childRoute: Ref<boolean>,
  route: RouteLocationNormalizedLoaded | RouteLocationNormalized,
) {
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
      CredentialManagerTypeEnum,
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

        proxy
          .$confirm(msg, '删除命名空间', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning',
            dangerouslyUseHTMLString: true,
          })
          .then(() => {
            deletings.value[name] = true;

            deleteNamespace(name)
              .then(() => {
                proxy.$success('删除成功');

                delete deletings.value[name];

                // 同步命名空间列表（vuex状态）
                proxy.mutateNamespaceDeletion(name);
              })
              .catch((err: Error) => {
                proxy.$throw(err, proxy);

                delete deletings.value[name];
              });
          })
          .catch(() => {
          });
      },
    };
  },
});
</script>

<style scoped lang="less">
.secret-key-ns-manager {
  padding: 15px;
  background-color: #ffffff;
  margin-bottom: 20px;

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
        color: #b5bdc6;
      }

      &.add {
        margin: 0.5%;
        width: 19%;
        min-width: 260px;
        height: 170px;
        background-color: #ffffff;
        border: 1px dashed #b5bdc6;
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
      width: 100%;
      display: flex;
      flex-wrap: wrap;

      .local-item,
      .vault-item {
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

        .wrapper {
          padding: 15px;
          border: 1px solid transparent;
          height: 138px;

          &:hover {
            border-color: #096dd9;
          }

          .name {
            font-size: 20px;
            font-weight: bold;
            color: #082340;

            &:hover {
              color: #096dd9;
            }
          }

          .description {
            font-size: 13px;
            color: #6b7b8d;

            .text-viewer {
              height: 90px;
            }
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

      .vault-item {
        .wrapper {
          display: flex;
          flex-direction: column;
          align-items: center;

          .vault-icon {
            width: 64px;
            height: 64px;
            margin: 20px 0px;
            background: url('@/assets/svgs/secret-key/key-title-icon.svg');
          }

          a {
            &:nth-child(2) {
              align-self: stretch;
            }
          }

          .vault-name {
            font-size: 20px;
            font-weight: bold;
            color: #082340;
            cursor: pointer;

            ::v-deep(.jm-text-viewer) {
              width: 100%;

              .content {
                .text-line {
                  &:last-child {
                    text-align: center;

                    &::after {
                      display: none;
                    }
                  }
                }
              }
            }

            &:hover {
              color: #096dd9;
            }
          }
        }

      }
    }

  }
}
</style>
