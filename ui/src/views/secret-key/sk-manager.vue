<template>
  <div class="secret-key-sk-manager" v-loading="loading">
    <div v-show="!loading">
      <div class="namespace">
        <router-link :to="{name:'secret-key'}">
          <span class="key-title-icon"></span>
          <span class="name">{{ ns }}</span>
        </router-link>
        <span class="divider"></span>
        <span>密钥列表</span>
        <span class="desc">（{{ keys.length }}）</span>
      </div>
      <div class="keys">
        <div class="content">
          <button class="add" @click="add">
            <i class="jm-icon-button-add"></i>
            <div class="label">新增密钥</div>
          </button>
          <div class="item" v-for="{ id, name } of keys" :key="id">
            <div class="wrapper">
              <div class="name">
                <jm-text-viewer :value="name"/>
              </div>
            </div>
            <div class="operation">
              <jm-tooltip content="删除" placement="top">
                <button
                  :class="{ del: true, doing: deletings[name] }"
                  @click="del(name)"
                  @keypress.enter.prevent
                ></button>
              </jm-tooltip>
            </div>
          </div>
        </div>
      </div>
    </div>
    <sk-editor
      v-if="creationActivated"
      @closed="creationActivated = false"
      :ns="ns"
      @completed="handleKeyAdd"
    />
  </div>
</template>

<script lang="ts">
import { defineComponent, getCurrentInstance, onBeforeMount, ref } from 'vue';
import SkEditor from './sk-editor.vue';
import { deleteSecretKey } from '@/api/secret-key';
import { fetchNamespaceDetail, listSecretKey } from '@/api/view-no-auth';
import { v4 as uuidv4 } from 'uuid';

interface IKeyType {
  id: string;
  name: string;
}

export default defineComponent({
  components: {
    SkEditor,
  },
  props: {
    ns: {
      type: String,
      required: true,
    },
  },
  setup(props: any) {
    const { proxy } = getCurrentInstance() as any;
    const description = ref<string>('无');
    const keys = ref<IKeyType[]>([]);
    const loading = ref<boolean>(false);
    const creationActivated = ref<boolean>(false);
    const deletings = ref<{ [name: string]: boolean }>({});

    // 初始化密钥key列表
    onBeforeMount(async () => {
      try {
        loading.value = true;

        const { description: desc } = await fetchNamespaceDetail(props.ns);
        description.value = (desc || '无').replace(/\n/g, '<br/>');

        keys.value = (await listSecretKey(props.ns)).map(item => ({
          id: uuidv4(),
          name: item,
        }));
      } catch (err) {
        proxy.$throw(err, proxy);
      } finally {
        loading.value = false;
      }
    });

    return {
      description,
      keys,
      loading,
      creationActivated,
      deletings,
      handleKeyAdd: async () => {
        try {
          loading.value = true;
          keys.value = (await listSecretKey(props.ns)).map(item => ({
            id: uuidv4(),
            name: item,
          }));
        } catch (err) {
          proxy.$throw(err, proxy);
        } finally {
          loading.value = false;
        }
      },
      add: () => {
        creationActivated.value = true;
      },
      del: (name: string) => {
        if (deletings.value[name]) {
          return;
        }

        let msg = '<div>确定要删除密钥吗?</div>';
        msg += `<div style="margin-top: 5px; font-size: 12px; line-height: normal;">名称：${name}</div>`;

        proxy
          .$confirm(msg, '删除密钥', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning',
            dangerouslyUseHTMLString: true,
          })
          .then(() => {
            deletings.value[name] = true;

            deleteSecretKey(props.ns, name)
              .then(() => {
                proxy.$success('删除成功');

                delete deletings.value[name];

                const index = keys.value.findIndex(item => item.name === name);
                keys.value.splice(index, 1);
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
.secret-key-sk-manager {
  min-height: calc(100vh - 145px);
  font-size: 14px;
  color: #333333;
  margin-bottom: 20px;
  background-color: #ffffff;

  .namespace {
    padding: 30px 0 10px calc(10px + 0.5%);
    background-color: #ffffff;
    display: flex;
    align-items: center;
    font-size: 16px;
    color: #082340;
    font-weight: 400;

    a {
      display: flex;
      align-items: center;
      max-width: 80%;

      &:hover {
        .key-title-icon {
          background: url('@/assets/svgs/secret-key/secret-key-icon-active.svg') no-repeat;
        }
      }

      .key-title-icon {
        flex-shrink: 0;
        width: 20px;
        height: 20px;
        background: url('@/assets/svgs/secret-key/secret-key-icon.svg') no-repeat;
        margin-right: 5px;
      }

      .name {
        white-space: nowrap;
        text-overflow: ellipsis;
        overflow: hidden;
      }
    }

    .divider {
      width: 1px;
      height: 16px;
      background-color: #8E9AA7;
      margin: 0 10px;
    }

    .desc {
      font-weight: normal;
      color: #8E9AA7;
    }
  }

  .keys {
    box-sizing: border-box;
    padding: 0 10px;

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

      .item {
        box-sizing: border-box;
        position: relative;
        margin: 0.5%;
        width: 19%;
        min-width: 260px;
        height: 180px;
        border: 1px solid #E7ECF1;
        background-image: url('@/assets/svgs/secret-key/key-icon.svg');
        background-repeat: no-repeat;
        background-position: center 40px;
        background-color: #ffffff;
        border-radius: 4px;

        &:hover {
          box-shadow: 0 0 12px 4px #EDF1F8;
          border: 1px solid transparent;

          .operation {
            display: block;
          }
        }

        .wrapper {
          padding: 15px;
          border: 1px solid transparent;
          height: 138px;

          .name {
            margin-top: 100px;
            font-size: 16px;
            text-align: center;
            font-weight: 400;
            color: #082340;
          }

          ::v-deep(.jm-text-viewer) {
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
  }

  ::v-deep(.el-dialog) {
    .el-dialog__footer {
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

    .el-form {
      .el-textarea__inner {
        height: 74px;
        resize: none;
      }
    }
  }
}
</style>
