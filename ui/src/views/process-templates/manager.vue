<template>
    <div class="process-templates">
        <div class="right-top-btn">
            <router-link :to="{name: 'index'}">
                <jm-button class="jm-icon-button-cancel" size="small">取消</jm-button>
            </router-link>
            <router-link :to="{name: 'index'}">
                <jm-button type="primary" class="jm-icon-button-next" size="small">下一步</jm-button>
            </router-link>
        </div>
        <div class="process-templates-flex">
            <!-- left -->
            <div class="ptf-l">
                <div class="ptf-l-t">
                    <i></i>
                    <span>流程模版</span>
                </div>

                <div class="ptf-l-b">
                    <p>分类</p>
                    <jm-scrollbar max-height="50vh">
                        <ul>
                            <li v-for="i in categoriesList"
                                :key="i.id"
                            >{{ i.name }}</li>
                        </ul>
                    </jm-scrollbar>
                </div>
            </div>
            <!-- right -->
            <div class="ptf-r">
                <div class="ptf-r-t">
                    <jm-form :model="processTemplatesForm" :rules="rules"  label-position="top">
                        <jm-form-item prop="name" label="项目名称">
                            <jm-input placeholder="请输入项目名称" style="width:65%" v-model="processTemplatesForm.name"></jm-input>
                        </jm-form-item>
                    </jm-form>
                </div>
                <div class="ptf-r-b">
                    <div class="ptf-r-b-t">
                        <jm-input v-model="search" placeholder="请输入模版名称">
                            <template #prefix>
                                <i class="el-input__icon el-icon-search"></i>
                            </template>
                        </jm-input>
                    </div>
                    <div class="ptf-r-b-b">
                        <p>选择流程模版</p>
                        <ul>
                            <li v-for="item in templatesList"
                                :key="item.id">
                                <div class="templates-tit">
                                    <p>{{ item.name }}</p>
                                    {{ item.dsl}}
                                </div>
                                <jm-workflow-viewer :dsl="item.dsl"
                                                    :node-infos="nodeInfos"
                                                    :tasks="taskRecords"
                                                    :triggerType="item.type"
                                                    />
                            </li>
                        </ul>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, computed, reactive, ref } from 'vue';
import { workflowTemplatesCategories, viewWorkflowTemplates } from '@/api/process-templates';
import { ICategories, IWorkflowTemplateViewingDto, ITemplateList, IContent } from '@/api/dto/process-templates';
import { useStore } from 'vuex';
import { namespace } from '@/store/modules/workflow-execution-record';
import { IState } from '@/model/modules/workflow-execution-record';
import { ITaskExecutionRecordVo, INodeInfoVo } from '@/api/dto/workflow-execution-record';

export default defineComponent({

  setup() {
    const state = useStore().state[namespace] as IState;
    const categoriesList = reactive<ICategories[]>([]);
    const processTemplatesForm = reactive<{ name:string}>({
      name:'',
    });
    const workflowTemplates = reactive<IWorkflowTemplateViewingDto>({
      pageNum: 0,
      pageSize: 10,
    //   name: '',
    //   templateCategoryId: 0,
    });
    const templatesList = reactive<IContent[]>([]);
    const search = ref<string>('');
    const rules = {
      name:[
        { 
          required: true,
          message: '请输入项目名称',
          trigger: 'blur',
        },
      ],
    };
    workflowTemplatesCategories().then((res:ICategories[]) => {
      categoriesList.push(...res);
          
    });
    viewWorkflowTemplates(workflowTemplates).then((res:ITemplateList) => {
      templatesList.push(...res.content);
      console.log(res);
        
    });
    console.log(state, 'state');
    
    return { 
      categoriesList,
      processTemplatesForm,
      rules,
      search,
      templatesList,
      nodeInfos: computed<INodeInfoVo[]>(() => state.recordDetail.nodeInfos),
      taskRecords: computed<ITaskExecutionRecordVo[]>(() => state.recordDetail.taskRecords),
    };
  },
});
</script>

<style lang="less">

.process-templates{
    padding: 16px 0px 25px 0px;
    li{
        list-style:none;
    }
    .right-top-btn {
        position: fixed;
        right: 20px;
        top: 78px;
        .jm-icon-button-next{
            margin-left: 10px;
        }
        .jm-icon-button-next::before,
        .jm-icon-button-cancel::before {
            font-weight: bold;
            
        }
    }

    .process-templates-flex{
        display:flex;
        .ptf-l{
            .ptf-l-t{
                display:flex;
                width: 264px;
                height: 82px;
                padding-left: 20px;
                margin-bottom:30px;
                align-items:center;
                background-image: url("@/assets/svgs/process-templates/bj.png");
                &>i{
                    display:inline-block;
                    width: 54px;
                    height: 54px;
                    margin-right: 12px;
                    background-image: url("@/assets/svgs/process-templates/process-templates.svg");
                }
                &>span{
                    font-weight: 500;
                    color: #012C53;
                    font-size: 16px;
                }
            }

            .ptf-l-b{
                padding-left:20px;
                font-size: 16px;
                &>p{
                    color: #6B7B8D;
                    margin-bottom:22px;
                }
                ul{
                    padding-left:40px;
                    li{
                        height: 24px;
                        color: #082340;
                        margin-bottom:7%;
                        
                    }
                    li:hover{
                        color: #096DD9;
                        cursor: pointer;
                    }
                }
                
            }
        }

        .ptf-r{
            margin-left: 30px;
            flex-grow:1;
            .ptf-r-t{
                background-color: #fff;
                padding:20px 30px 24px 30px;
                margin-bottom:20px;
                ::v-deep(.el-form-item__label){
                    padding:0;
                }
                ::v-deep(.el-form-item) {
                    margin:0;
                }
            }

            .ptf-r-b{
                .ptf-r-b-t{
                    background-color:#F6FAFE;
                    font-size: 14px;
                    color: #7B8C9C;
                    height: 66px;
                    padding: 15px 30px 15px 30px;
                    box-sizing: border-box;
                    // ::v-deep(.el-input){
                    //     line-height: 36px;
                    // }
                    ::v-deep(.el-input__inner){
                        border:none;
                    }
                }

                .ptf-r-b-b{
                    padding: 20px 24px 20px 24px;
                    background-color: #fff;
                    &>p{
                        font-size: 14px;
                        color: #082340;
                        margin-bottom:20px;
                    }
                    li{
                        width: 100%;
                        border-radius: 4px;
                        border: 1px solid #B9CFE6;
                        // height: 183px;
                        position: relative;
                        margin-bottom:20px;
                        .templates-tit{
                            position: absolute;
                            top: 20px;
                            left: 20px;
                            font-size: 16px;
                            color: #3F536E;
                        }
                    }
                }
                
            }
        }
    }

}
</style>
