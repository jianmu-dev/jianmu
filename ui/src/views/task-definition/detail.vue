<template>
  <div class="task-definition-detail">
    <div class="right-top-btn">
      <router-link to="/task-definition">
        <jm-button type="primary" class="jm-icon-button-cancel" size="small">关闭</jm-button>
      </router-link>
    </div>

    <div class="basic-section">
      <div class="medium">名称：<span class="param-value">{{ detail.name }}</span></div>
      <div class="medium">Ref：<span class="param-value">{{ detail.ref }}</span></div>
      <div class="small">版本号：<span class="param-value">{{ detail.version }}</span></div>
      <div class="description large">
        <div>描述：</div>
        <div class="param-value" v-html="detail.description"></div>
      </div>
      <div class="large">执行结果文件：<span class="param-value">{{ detail.resultFile }}</span></div>
      <div class="medium">Worker类型：<span class="param-value">{{ detail.type }}</span></div>
    </div>
    <div class="system-param-section" v-if="detail.type === 'DOCKER'">
      <div class="title">容器参数</div>
      <div class="content">
        <div class="main-params">
          <div class="medium">name：<span class="param-value">{{ detail.spec.name || '无' }}</span></div>
          <div class="medium">hostName：<span class="param-value">{{ detail.spec.hostName || '无' }}</span></div>
          <div class="medium">domainName：<span class="param-value">{{ detail.spec.domainName || '无' }}</span></div>
          <div class="medium">image：<span class="param-value">{{ detail.spec.image }}</span></div>
        </div>
        <div class="other-params">
          <jm-tabs v-model="systemParamTabActiveName">
            <jm-tab-pane name="command" lazy>
              <template #label>
                <div class="tab">Command</div>
              </template>
              <div class="tab-content">
                <div class="medium">command：<span
                  class="param-value">{{ detail.spec.cmd && detail.spec.cmd.length > 0 ? detail.spec.cmd : '无' }}</span>
                </div>
                <div class="medium">entrypoint：<span
                  class="param-value">{{
                    detail.spec.entrypoint && detail.spec.entrypoint.length > 0 ? detail.spec.entrypoint : '无'
                  }}</span>
                </div>
                <div class="medium">workingDir：<span class="param-value">{{ detail.spec.workingDir || '无' }}</span>
                </div>
                <div class="medium">user：<span class="param-value">{{ detail.spec.user || '无' }}</span></div>
              </div>
            </jm-tab-pane>
          </jm-tabs>
        </div>
      </div>
    </div>
    <div class="business-param-section">
      <div class="title">业务参数</div>
      <div class="content">
        <div class="subtitle">输入参数</div>
        <jm-table
          :data="detail.inputParameters"
          border>
          <jm-table-column
            label="参数唯一标识"
            align="center"
            prop="ref">
          </jm-table-column>
          <jm-table-column
            label="参数名称"
            align="center"
            prop="name">
          </jm-table-column>
          <jm-table-column
            label="参数类型"
            align="center"
            prop="type">
          </jm-table-column>
          <jm-table-column
            label="参数值"
            align="center"
            prop="value">
          </jm-table-column>
          <jm-table-column
            label="参数描述"
            align="center"
            prop="description">
          </jm-table-column>
        </jm-table>
        <div class="subtitle separator">输出参数</div>
        <jm-table
          :data="detail.outputParameters"
          border>
          <jm-table-column
            label="参数唯一标识"
            align="center"
            prop="ref">
          </jm-table-column>
          <jm-table-column
            label="参数名称"
            align="center"
            prop="name">
          </jm-table-column>
          <jm-table-column
            label="参数类型"
            align="center"
            prop="type">
          </jm-table-column>
          <jm-table-column
            label="参数值"
            align="center"
            prop="value">
          </jm-table-column>
          <jm-table-column
            label="参数描述"
            align="center"
            prop="description">
          </jm-table-column>
        </jm-table>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, onBeforeMount, ref } from 'vue';
import { ITaskDefinitionVersionDetailVo } from '@/api/dto/task-definition';
import { fetchVersionDetail } from '@/api/task-definition';

export default defineComponent({
  props: {
    // 任务定义ref
    taskDefRef: String,
    // 任务定义版本
    taskDefVersion: String,
  },
  setup(props: any) {
    const detail = ref<ITaskDefinitionVersionDetailVo>({
      name: '',
      ref: '',
      type: '',
      version: '',
      resultFile: '',
      inputParameters: [],
      outputParameters: [],
      spec: {
        image: '',
      },
    });

    onBeforeMount(async () => {
      const temp = await fetchVersionDetail(props.taskDefRef, props.taskDefVersion);
      const description: string = (temp.description || '无').replaceAll('\n', '<br/>');
      detail.value = {
        ...temp,
        description,
      };
    });

    return {
      detail,
      systemParamTabActiveName: ref<string>('command'),
    };
  },
});
</script>

<style scoped lang="less">
.task-definition-detail {
  font-size: 14px;
  color: #333333;
  margin-bottom: 25px;

  .param-value {
    color: #606266;
  }

  .right-top-btn {
    position: fixed;
    right: 20px;
    top: 78px;

    .jm-icon-button-cancel::before {
      font-weight: bold;
    }
  }

  .basic-section {
    padding: 16px 20% 0 24px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    flex-wrap: wrap;
    background-color: #FFFFFF;
    border-radius: 4px;
    border: 1px solid #E6EBF2;

    > div {
      margin-bottom: 16px;
    }

    .large {
      width: 100%;
    }

    .medium {
      width: 30%;
    }

    .small {
      width: 20%;
    }

    .description {
      display: flex;
    }
  }

  .system-param-section {
    padding: 16px 24px;
    background-color: #F6F8FB;
    border-radius: 4px;

    .title {
      margin-bottom: 16px;
      font-weight: bold;
    }

    .content {
      padding: 0 24px 16px 24px;
      background-color: #FFFFFF;
      border: 1px solid #EEF0F7;

      .large {
        width: 100%;
      }

      .medium {
        width: 33%;
      }

      .main-params {
        display: flex;
        justify-content: space-between;
        align-items: center;
        flex-wrap: wrap;

        > div {
          margin-top: 16px;
        }
      }

      .other-params {
        margin-top: 20px;

        .tab {
          width: 116px;
          height: 37px;
          text-align: center;
          background-color: #F6F8FB;
          border-radius: 6px 6px 0 0;
          border: 1px solid #EEF0F7;
        }

        .tab-content {
          padding: 0 24px 16px;
          border: 1px solid #EEF0F7;
          display: flex;
          justify-content: space-between;
          align-items: center;
          flex-wrap: wrap;

          > div {
            margin-top: 16px;
          }
        }

        ::v-deep(.el-tabs) {
          .el-tabs__nav-wrap {
            box-shadow: none;
          }

          .el-tabs__nav-scroll {
            line-height: normal;
          }

          .el-tabs__item {
            padding-left: 0;
            padding-right: 0;
          }
        }
      }
    }
  }

  .business-param-section {
    margin-top: 16px;
    background-color: #FFFFFF;
    border-radius: 4px;

    .title {
      padding: 16px 24px;
      font-weight: bold;
      border-bottom: 1px solid #ECEEF6;
    }

    .content {
      padding: 0 24px 16px;

      .subtitle {
        padding: 16px 0;
        font-weight: 400;

        &.separator {
          margin-top: 16px;
          border-top: 1px solid #ECEEF6;
        }
      }
    }
  }
}
</style>
