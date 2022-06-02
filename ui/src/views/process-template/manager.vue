<template>
  <div class="process-template" v-scroll.current="showMore">
    <div class="right-top-btn">
      <router-link :to="{ name: 'index' }">
        <jm-button class="jm-icon-button-cancel" size="small">取消</jm-button>
      </router-link>

      <jm-button
        @click="next"
        type="primary"
        class="jm-icon-button-next"
        size="small"
      >下一步
      </jm-button
      >
    </div>
    <div class="process-template-flex">
      <!-- left -->
      <div v-if="categoriesList.length > 0" class="ptf-l">
        <div class="ptf-l-t">
          <i></i>
          <span>流程模版</span>
        </div>

        <div class="ptf-l-b">
          <p>分类</p>
          <jm-scrollbar max-height="50vh">
            <ul>
              <li
                v-for="i in categoriesList"
                :key="i.id"
                :class="{ click: templatesClickData.classifyId === i.id }"
                @click="classifyClick(i)"
              >
                <img v-if="i.icon" :src="i.icon"/>
                <i v-else>{{ i.name[0].toUpperCase() }}</i>
                <jm-text-viewer :value="i.name"/>
              </li>
            </ul>
          </jm-scrollbar>
        </div>
      </div>
      <!-- right -->
      <div
        class="ptf-r"
        :style="{ marginLeft: categoriesList.length > 0 ? '30px' : '0px' }"
      >
        <div class="ptf-r-t">
          <jm-form
            ref="processTemplatesDom"
            :model="processTemplatesForm"
            :rules="rules"
            label-position="top"
            @submit.prevent
          >
            <jm-form-item prop="processTemplatesName" label="项目名称">
              <jm-input
                placeholder="请输入项目名称"
                style="width: 65%"
                v-model="processTemplatesForm.processTemplatesName"
              ></jm-input>
            </jm-form-item>
          </jm-form>
        </div>
        <div class="ptf-r-b">
          <div class="ptf-r-b-t">
            <jm-input
              @change="searchsTemplate"
              v-model="workflowTemplates.name"
              placeholder="请输入模版名称"
            >
              <template #prefix>
                <i class="el-input__icon el-icon-search"></i>
              </template>
            </jm-input>
          </div>

          <div class="ptf-r-b-b">
            <jm-empty v-if="templatesList.length <= 0 && !templateLoading"/>
            <template v-else>
              <p>选择流程模版</p>
              <ul v-loading="templateLoading">
                <li
                  v-for="item in templatesList"
                  :key="item.id"
                  :class="{ click: templatesClickData.templateId === item.id }"
                  @click="templatesItem(item)"
                >
                  <div class="template-tit">
                    <h3
                      :style="{
                        backgroundColor:
                          '#' +
                          (
                            '00000' +
                            (
                              (Number('0.' + item.id) * 0x1000000) <<
                              0
                            ).toString(16)
                          ).substr(-6),
                      }"
                      class="template-tit-h3"
                    >
                      {{ item.type[0] }}
                    </h3>
                    <p>{{ item.name }}</p>
                  </div>
                  <jm-workflow-viewer
                    style="height: 360px; pointer-events: none"
                    :dsl="item.dsl"
                    readonly
                    :node-infos="item.nodeDefs"
                    :trigger-type="TriggerTypeEnum.MANUAL"
                  />
                </li>
              </ul>
              <div v-if="isShowMore" @click="showMore" class="show-more">
                <span>显示更多</span>
                <i
                  class="btm-down"
                  :class="{ 'btn-loading': bottomLoading }"
                ></i>
              </div>
            </template>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, reactive, ref } from 'vue';
import {
  workflowTemplateCategories,
  viewWorkflowTemplate,
} from '@/api/process-template';
import {
  ICategoriesVo,
  ITemplateListVo,
  IContentVo,
} from '@/api/dto/process-template';
import { TriggerTypeEnum } from '@/api/dto/enumeration';
import {
  IWorkflowTemplateViewingForm,
  ITemplatesCLickData,
  IProcessTemplatesForm,
} from '@/model/modules/process-template';
import { useRouter } from 'vue-router';
import { START_PAGE_NUM } from '@/utils/constants';

export default defineComponent({
  name: 'process-template',
  props: {
    processTemplatesName: String,
  },
  setup() {
    const router = useRouter();
    const categoriesList = reactive<ICategoriesVo[]>([]);
    const processTemplatesForm = reactive<IProcessTemplatesForm>({
      processTemplatesName: '',
    });
    const workflowTemplates = reactive<IWorkflowTemplateViewingForm>({
      pageNum: START_PAGE_NUM,
      pageSize: 10,
      name: '',
      templateCategoryId: undefined,
    });
    const templatesList = reactive<IContentVo[]>([]);
    const rules = {
      processTemplatesName: [
        {
          required: true,
          message: '请输入项目名称',
          trigger: 'change',
        },
      ],
    };
    const templatesClickData = reactive<ITemplatesCLickData>({
      templateId: undefined,
      templateName: '',
      classifyId: undefined,
    });
    const isShowMore = ref<boolean>(false);
    const templateLoading = ref<boolean>(true);
    const bottomLoading = ref<boolean>(false);
    const processTemplatesDom = ref<any>(null);
    const totalPages = ref<number>(0);
    let classifyClickKey = true;
    // 流程模版
    const getTemplatesList = (
      workflowTemplates: IWorkflowTemplateViewingForm,
    ) => {
      templateLoading.value = true;
      isShowMore.value = false;
      viewWorkflowTemplate(workflowTemplates)
        .then((res: ITemplateListVo) => {
          templatesList.push(...res.content);
          totalPages.value = res.totalPages;
          if (workflowTemplates.pageNum < totalPages.value) {
            isShowMore.value = true;
          } else {
            isShowMore.value = false;
          }
        })
        .catch((err: Error) => {
          console.warn(err.message);
        })
        .finally(() => {
          templateLoading.value = false;
          bottomLoading.value = false;
          classifyClickKey = true;
        });
    };
    // 列表
    workflowTemplateCategories()
      .then((res: ICategoriesVo[]) => {
        categoriesList.push(...res);
        getTemplatesList(workflowTemplates);
      })
      .catch((err: Error) => {
        console.warn(err.message);
      })
      .finally(() => {
        templateLoading.value = false;
      });

    const searchsTemplate = (name: string) => {
      workflowTemplates.name = name;
      workflowTemplates.pageNum = 1;
      if (name === '') {
        workflowTemplates.templateCategoryId = templatesClickData.classifyId;
      }
      templatesList.length = 0;
      getTemplatesList(workflowTemplates);
    };

    const showMore = () => {
      if (bottomLoading.value) return;
      workflowTemplates.pageNum++;
      bottomLoading.value = true;
      if (workflowTemplates.pageNum > totalPages.value) {
        return;
      }
      getTemplatesList(workflowTemplates);
    };
    const next = () => {
      processTemplatesDom.value.validate((valid: boolean) => {
        if (!valid) return false;
        router.push({
          name: 'create-project',
          query: {
            processTemplatesName: processTemplatesForm.processTemplatesName,
            templateId: templatesClickData.templateId,
            source: 'processTemplates',
          },
        });
      });
    };

    return {
      searchsTemplate,
      next,
      processTemplatesDom,
      bottomLoading,
      isShowMore,
      showMore,
      categoriesList,
      processTemplatesForm,
      rules,
      TriggerTypeEnum,
      templatesList,
      templateLoading,
      workflowTemplates,
      classifyClick(i: ICategoriesVo) {
        if (!classifyClickKey) return;
        classifyClickKey = false;
        if (i.id === templatesClickData.classifyId) {
          templatesClickData.classifyId = undefined;
          workflowTemplates.templateCategoryId = undefined;
        } else {
          templatesClickData.classifyId = i.id;
          workflowTemplates.templateCategoryId = i.id;
        }
        templatesList.length = 0;
        isShowMore.value = true;
        workflowTemplates.pageNum = 1;
        workflowTemplates.name = '';
        getTemplatesList(workflowTemplates);
      },
      templatesClickData,
      templatesItem(item: IContentVo) {
        if (templatesClickData.templateId === item.id) {
          templatesClickData.templateId = undefined;
          templatesClickData.templateName = '';
          if (processTemplatesForm.processTemplatesName === item.name) {
            processTemplatesForm.processTemplatesName = '';
          }
        } else {
          templatesClickData.templateId = item.id;
          // 和上一次不一样就说明修改过
          if (
            processTemplatesForm.processTemplatesName ===
            templatesClickData.templateName ||
            processTemplatesForm.processTemplatesName === ''
          ) {
            processTemplatesForm.processTemplatesName = item.name;
          }
          templatesClickData.templateName = item.name;
        }
      },
    };
  },
});
</script>

<style scoped lang="less">
.process-template {
  padding: 16px 0px 25px 0px;
  height: calc(100vh - 170px);

  li {
    list-style: none;
  }

  .right-top-btn {
    position: fixed;
    right: 20px;
    top: 78px;

    .jm-icon-button-next {
      margin-left: 10px;
    }

    .jm-icon-button-next::before,
    .jm-icon-button-cancel::before {
      font-weight: bold;
    }
  }

  .process-template-flex {
    display: flex;

    .ptf-l {
      background-color: #fff;

      .ptf-l-t {
        display: flex;
        width: 264px;
        height: 82px;
        padding-left: 20px;
        align-items: center;
        background-image: url('@/assets/images/process-template/bj.png');

        & > i {
          display: inline-block;
          width: 54px;
          height: 54px;
          margin-right: 12px;
          background-image: url('@/assets/svgs/process-template/process-template.svg');
        }

        & > span {
          font-weight: 500;
          color: #012c53;
          font-size: 16px;
        }
      }

      .ptf-l-b {
        padding-left: 20px;
        font-size: 16px;
        padding-top: 30px;
        border-top: 1px solid #eff4f9;

        & > p {
          color: #6b7b8d;
          margin-bottom: 22px;
        }

        ul {
          padding-left: 15px;

          .click {
            color: #096dd9;
            cursor: pointer;
          }

          li {
            width: 100%;
            height: 24px;
            color: #082340;
            margin-bottom: 7%;
            display: flex;
            align-items: center;

            & > img {
              margin-right: 14px;
              width: 24px;
              height: 24px;
            }

            & > i {
              display: flex;
              margin-right: 14px;
              width: 24px;
              height: 24px;
              justify-content: center;
              align-items: center;
              border-radius: 50%;
              background-color: #7b8c9c;
              color: #fff;
              font-size: 13px;
              font-style: inherit;
            }

            & > span {
              max-width: 200px;
            }
          }

          li:hover {
            color: #096dd9;
            cursor: pointer;
          }
        }
      }
    }

    .ptf-r {
      flex-grow: 1;

      .ptf-r-t {
        background-color: #fff;
        padding: 20px 30px 24px 30px;
        margin-bottom: 20px;

        ::v-deep(.el-form-item__label) {
          padding: 0;
        }

        ::v-deep(.el-form-item) {
          margin: 0;
        }
      }

      .ptf-r-b {
        .ptf-r-b-t {
          background-color: #f6fafe;
          font-size: 14px;
          color: #7b8c9c;
          height: 66px;
          padding: 15px 30px 15px 30px;
          box-sizing: border-box;

          ::v-deep(.el-input__inner) {
            border: none;
          }
        }

        .ptf-r-b-b {
          padding: 20px 24px 20px 24px;
          background-color: #fff;
          min-height: 220px;

          & > p {
            font-size: 14px;
            color: #082340;
            margin-bottom: 20px;
          }

          ul {
            min-height: 220px;
            position: relative;

            .click {
              border: 1px solid #096dd9;
              box-shadow: 0px 0px 16px 4px #dce3ef;
            }

            li {
              border-radius: 4px;
              border: 1px solid #b9cfe6;
              max-height: 360px;
              position: relative;
              margin-bottom: 20px;
              cursor: pointer;

              &:hover {
                border: 1px solid #096dd9;
                box-shadow: 0px 0px 16px 4px #dce3ef;
              }

              .template-tit {
                position: absolute;
                top: 20px;
                left: 20px;
                font-size: 16px;
                color: #3f536e;
                display: flex;
                align-items: center;

                .template-tit-h3 {
                  border-radius: 50%;
                  width: 30px;
                  height: 30px;
                  display: flex;
                  justify-content: center;
                  align-items: center;
                  color: #fff;
                  margin-right: 14px;
                  font-size: 17px;
                }
              }
            }
          }

          .show-more {
            color: #7b8c9c;
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
      }
    }
  }
}
</style>
