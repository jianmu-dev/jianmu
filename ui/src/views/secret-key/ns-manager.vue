<template>
  <router-view v-if="childRoute"/>
  <div v-else class="secret-key-ns-manager" v-loading="loading">
    <div class="title">
      <span>命名空间</span>
      <span class="desc">（{{ namespaces.length }}）</span>
    </div>
    <div class="content">
      <button class="add" @click="add">
        <i class="jm-icon-button-add"></i>
        <div class="label">新增命名空间</div>
      </button>
      <template v-if="credentialManagerType === CredentialManagerTypeEnum.LOCAL">
        <div class="vault-item" v-for="ns of namespaces" :key="ns.name" @click="toNs(ns.name)">
          <div class="wrapper">
            <div class="vault-icon"></div>
            <div class="vault-name">
              <jm-text-viewer :value="ns.name"/>
            </div>
          </div>
          <jm-tooltip content="删除" placement="top">
            <div class="operation">
              <button
                :class="{ del: true, doing: deletings[ns.name] }"
                @click.stop="del(ns.name)"
              ></button>
            </div>
          </jm-tooltip>
        </div>
      </template>
      <template v-else>
        <div class="vault-item" v-for="ns of namespaces" :key="ns.name" @click="toNs(ns.name)">
          <div class="wrapper">
            <div class="vault-icon"></div>
            <div class="vault-name">
              <jm-text-viewer :value="ns.name"/>
            </div>
          </div>
          <jm-tooltip content="删除" placement="top">
            <div class="operation">
              <button
                :class="{ del: true, doing: deletings[ns.name] }"
                @click.stop="del(ns.name)"
              ></button>
            </div>
          </jm-tooltip>
        </div>
      </template>
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
  useRoute, useRouter,
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
    const router = useRouter();
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
      toNs: async namespace => {
        await router.push({ name: 'manage-secret-key', params: { namespace } });
      },
    };
  },
});
</script>

<style scoped lang="less">
.secret-key-ns-manager {
  padding: 30px 10px 0;
  box-sizing: border-box;
  background-color: #ffffff;
  height: calc(100vh - 145px);
  margin-bottom: 20px;

  .title {
    margin-bottom: 10px;
    margin-left: 0.5%;
    font-size: 16px;
    font-weight: 500;
    color: #082340;

    .desc {
      font-weight: 400;
      font-size: 14px;
      color: #8E9AA7;
    }
  }

  .content {
    display: flex;
    flex-wrap: wrap;

    button {
      color: #096DD9;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;

      .label {
        width: 100%;
        text-align: center;
        font-size: 14px;
      }

      &.add {
        font-size: 36px;
        margin: 0.5%;
        width: 19%;
        min-width: 260px;
        height: 180px;
        background-color: #ffffff;
        border: 1px solid #E7ECF1;
        cursor: pointer;
        border-radius: 4px;

        i {
          &::before {
            font-weight: bold;
          }
        }
      }
    }

    .vault-item {
      cursor: pointer;
      position: relative;
      margin: 0.5%;
      width: 19%;
      min-width: 260px;
      height: 180px;
      background-color: #ffffff;
      border: 1px solid #E7ECF1;
      border-radius: 4px;
      box-sizing: border-box;

      .wrapper {
        height: 100%;
        display: flex;
        flex-direction: column;
        align-items: center;

        .vault-icon {
          width: 64px;
          height: 64px;
          margin: 35px 0 15px;
          background: url('@/assets/svgs/secret-key/key-title-icon.svg');
        }

        .vault-name {
          width: 90%;
          font-size: 16px;
          font-weight: 400;
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
        }
      }

      &:hover {
        box-shadow: 0 0 12px 4px #EDF1F8;
        border: 1px solid transparent;

        .wrapper {
          .vault-icon {
            background: url('@/assets/svgs/secret-key/key-title-icon-active.svg');
          }
        }

        .operation {
          display: block;
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

          &:hover {
            background-color: #eff7ff;
            border-radius: 2px;
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

  ::v-deep(.el-dialog__footer) {
    background-color: #fff;

    .el-button {
      border: none;
      padding: 8px 24px;
      border-radius: 2px;
      box-shadow: none;

      &:nth-child(2) {
        margin: 0 10px 0 20px;
      }
    }

    .el-button--default {
      background-color: #F5F5F5;
      color: #082340;

      &:hover {
        background-color: #EFF7FF;
        color: #0091FF;
      }
    }
  }
}
</style>
