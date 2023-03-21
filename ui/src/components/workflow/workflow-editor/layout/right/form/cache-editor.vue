<template>
  <div class="cache-editor">
    <jm-form-item :prop="`${index}.ref`" :rules="rules.ref">
      <div class="form-content">
        <jm-input
          v-model="cacheVal"
          placeholder="请输入缓存唯一标识"
          maxlength="30"
          @change="changeCacheVal"
          v-if="type === CacheTypeEnum.ADD"
        />
        <div class="echo-cache-container" v-else>
          <div v-if="isEdit" class="echo-cache">
            <div class="cache-content">
              {{ cacheVal }}
            </div>
            <i class="jm-icon-workflow-edit" @click="isEdit = false" />
          </div>
          <jm-input
            v-model="cacheVal"
            maxlength="30"
            placeholder="请输入缓存唯一标识"
            @change="changeCacheVal"
            v-else
          />
        </div>
        <i class="jm-icon-button-delete" @click="handleDelete" />
      </div>
    </jm-form-item>
  </div>
</template>

<script lang="ts">
import { ref, defineComponent, onUpdated, PropType } from 'vue';
import { CustomRule } from '../../../model/data/common';
import { CacheTypeEnum } from '../../../layout/right/cache-panel.vue';

export default defineComponent({
  props: {
    reference: {
      type: String,
      required: true,
    },
    type: {
      type: String as PropType<CacheTypeEnum>,
      required: true,
    },
    index: {
      type: Number,
      required: true,
    },
    rules: {
      type: Object as PropType<Record<string, CustomRule>>,
      required: true,
    },
  },
  emits: ['update:cache-ref', 'delete', 'change-cache'],
  setup(props, { emit }) {
    const cacheVal = ref<string>(props.reference);
    const isEdit = ref<boolean>(true);

    onUpdated(() => {
      if (cacheVal.value === props.reference) {
        return;
      }
      cacheVal.value = props.reference;
    });

    return {
      cacheVal,
      CacheTypeEnum,
      isEdit,
      handleDelete: () => emit('delete', props.index),
      changeCacheVal: () => {
        const oldVal = props.reference;
        emit('change-cache', cacheVal.value, oldVal, props.index);
      },
    };
  },
});
</script>

<style scoped lang="less">
.cache-editor {
  ::v-deep(.el-form-item) {
    .el-form-item__content {
      .form-content {
        display: flex;
        align-items: center;

        .el-input {
          margin-right: 12px;

          .el-input__inner {
            padding-left: 8px;
          }
        }

        .echo-cache-container {
          width: 100%;
          margin-right: 12px;

          .echo-cache {
            width: 334px;
            position: relative;
            background: rgba(240, 242, 245, 0.8);

            .cache-content {
              box-sizing: border-box;
              height: 36px;
              line-height: 20px;
              background: rgba(240, 242, 245, 0.8);
              border-radius: 4px;
              width: 334px;
              padding: 8px;
              overflow: hidden;
              text-overflow: ellipsis;
              white-space: nowrap;
            }

            &:hover {
              .cache-content {
                width: 315px;
                overflow: hidden;
                text-overflow: ellipsis;
                white-space: nowrap;
              }
              .jm-icon-workflow-edit {
                display: inline-block;
              }
            }

            .jm-icon-workflow-edit {
              width: 24px;
              height: 24px;
              line-height: 24px;
              color: #526579;
              display: none;

              position: absolute;
              right: 0;
              top: 6px;

              &:hover {
                cursor: pointer;
                color: #096dd9;
              }
            }
          }
        }

        .jm-icon-button-delete {
          display: inline-block;
          width: 24px;
          height: 24px;
          line-height: 24px;
          color: #526579;
          text-align: center;

          &:hover {
            cursor: pointer;
            background: #eff7ff;
            color: #096dd9;
            border-radius: 4px;
          }
        }
      }
    }
  }
}
</style>
