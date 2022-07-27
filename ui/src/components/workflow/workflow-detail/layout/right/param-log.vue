<template>
  <div class="jm-workflow-detail-param-log">
    <div class="tab-content">
      <jm-table
        :data="globalParams"
        :border="true"
        :cell-class-name="
          ({ row, columnIndex }) =>
            row.required && columnIndex === 0 ? 'required-cell' : ''
        "
      >
        <jm-table-column
          label="参数唯一标识"
          header-align="center"
          width="200px"
          prop="ref"
        >
          <template #default="scope">
            <div
              :style="{
                maxWidth: maxWidthRecord[scope.row.ref]
                  ? `${maxWidthRecord[scope.row.ref]}px`
                  : '100%',
              }"
            >
              <div class="text-viewer">
                <jm-text-viewer
                  :value="scope.row.ref"
                  class="value"
                  :tip-append-to-body="false"
                  @loaded="
                    ({ contentMaxWidth }) =>
                      getTotalWidth(contentMaxWidth, scope.row.ref)
                  "
                />
              </div>
              <jm-tooltip
                content="必填项"
                placement="top"
                :appendToBody="false"
                v-if="scope.row.required"
              >
                <img src="~@/assets/svgs/task-log/required.svg" alt="" />
              </jm-tooltip>
            </div>
          </template>
        </jm-table-column>
        <jm-table-column
          header-align="center"
          label="名称"
          width="120px"
          align="center"
          prop="name"
        >
        </jm-table-column>
        <jm-table-column
          header-align="center"
          label="参数类型"
          width="100px"
          align="center"
          prop="type"
        >
        </jm-table-column>
        <jm-table-column
          label="是/否必填"
          header-align="center"
          align="center"
          width="100px"
          prop="required"
        >
          <template #default="scope">
            <span :class="[scope.row.required ? 'is-required' : '']">
              {{ scope.row.required ? '是' : '否' }}
            </span>
          </template>
        </jm-table-column>
        <jm-table-column label="参数值" header-align="center">
          <template #default="scope">
            <param-value
              :value="scope.row.value"
              :tip-append-to-body="false"
              :type="scope.row.hidden? ParamTypeEnum.SECRET:scope.row.type"
            />
          </template>
        </jm-table-column>
      </jm-table>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, PropType, ref } from 'vue';
import ParamValue from '@/views/common/param-value.vue';
import { IGlobalParamseterVo } from '@/api/dto/project';
import { ParamTypeEnum } from '@/api/dto/enumeration';

export default defineComponent({
  components: { ParamValue },
  props: {
    globalParams: {
      type: Array as PropType<IGlobalParamseterVo[]>,
      required: true,
    },
  },
  setup() {
    const maxWidthRecord = ref<Record<string, number>>({});
    return {
      ParamTypeEnum,
      maxWidthRecord,
      getTotalWidth(width: number, ref: string) {
        maxWidthRecord.value[ref] = width;
      },
    };
  },
});
</script>

<style lang="less" scoped>
.tab-content {
  padding: 25px 30px 0;

  ::v-deep(.el-table) {
    overflow: visible;

    .el-table__body-wrapper {
      overflow: visible;

      td {
        &.required-cell {
          .cell {
            padding-right: 40px;
          }
        }

        .cell {
          padding: 0 20px;

          .text-viewer {
            display: flex;
            align-items: center;

            &.param-value {
              .value {
                width: 100%;
              }
            }

            .value {
              width: 100%;
            }
          }

          & > div {
            display: inline-block;
            width: 100%;
            position: relative;

            img {
              position: absolute;
              left: 100%;
              margin-left: 5px;
              bottom: 0
            }
          }
        }

        &.is-center {
          .cell {
            padding: 0px 10px;
          }
        }
      }

      .cell {
        overflow: visible;

        .is-required {
          color: #ff0000;
        }
      }
    }
  }

  .task-log {
    height: calc(100vh - 260px);
  }

  .tasks-log {
    height: calc(100vh - 330px);
  }

  .log {
    margin: 16px;
    position: relative;

    .loading {
      position: absolute;
      top: 0;
      right: 0;
      z-index: 1;

      ::v-deep(.el-button) {
        padding-left: 5px;
        padding-right: 5px;
      }
    }
  }

  .params {
    background-color: #ffffff;
    border-radius: 4px;
    color: #082340;

    .content {
      padding: 0 16px 16px 16px;

      .title {
        padding: 16px 0;
        font-weight: 400;

        &.separator {
          margin-top: 16px;
          border-top: 1px solid #eceef6;
        }
      }

      ::v-deep(.el-table) {
        th,
        td {
          color: #082340;
        }
      }
    }
  }
}
</style>
