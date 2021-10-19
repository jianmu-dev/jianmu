<template>
    <div class="process-templates">
        <div class="right-top-btn">
            <router-link :to="{name: 'index'}">
                <jm-button class="jm-icon-button-cancel" size="small">取消</jm-button>
            </router-link>
            
            <jm-button @click="next" type="primary" class="jm-icon-button-next" size="small">下一步</jm-button>
           
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
                    <jm-form ref="processTemplatesDom" :model="processTemplatesForm" :rules="rules"  label-position="top">
                        <jm-form-item prop="name" label="项目名称">
                            <jm-input placeholder="请输入项目名称" style="width:65%" v-model="processTemplatesForm.name"></jm-input>
                        </jm-form-item>
                    </jm-form>
                </div>
                <div class="ptf-r-b">
                    <div class="ptf-r-b-t">
                        <jm-input  @input="searchsTemplate"  v-model="search" placeholder="请输入模版名称">
                            <template #prefix>
                                <i class="el-input__icon el-icon-search"></i>
                            </template>
                        </jm-input>
                    </div>
                    <div  class="ptf-r-b-b">
                        <p>选择流程模版</p>
                        <jm-scrollbar v-loading="templateLoading"  height="736px">
                            <ul>
                                <li v-for="item in templatesListCopy"
                                    :key="item.id">
                                    <div class="templates-tit">
                                        <p>{{ item.name }}</p>
                                    </div>
                                    <jm-workflow-viewer style="max-height: 360px;"
                                                        :dsl="item.dsl"
                                                        :trigger-type="TriggerTypeEnum.MANUAL"
                                                    />
                                </li>
                                <p v-if="isShowMore" @click="showMore" class="show-more">
                                        <span>显示更多</span>
                                        <i class="btm-down" :class="{'btn-loading':bottomLoading}"></i>
                                </p>
                            </ul>
                           
                        </jm-scrollbar>
                        
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, reactive, ref } from 'vue';
import { workflowTemplatesCategories, viewWorkflowTemplates } from '@/api/process-templates';
import { ICategories, IWorkflowTemplateViewingDto, ITemplateList, IContent } from '@/api/dto/process-templates';
import { TriggerTypeEnum } from '@/api/dto/enumeration';


export default defineComponent({

  setup() {
    
    const categoriesList = reactive<ICategories[]>([]);
    const processTemplatesForm = reactive<{ name:string}>({
      name:'',
    });
    const workflowTemplates = reactive<{pageNum:number; pageSize:number; name:string;templateCategoryId:number}>({
      pageNum: 0,
      pageSize: 10,
    //   name: '',
    //   templateCategoryId: 0,
    });
    const templatesList:IContent[] = [];
    const templatesListCopy = reactive<IContent[]>([]);
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
    const isShowMore = ref<boolean>(true);
    const templateLoading = ref<boolean>(true);
    const bottomLoading = ref<boolean>(false);
    const processTemplatesDom = ref<any>(null);
    workflowTemplatesCategories().then((res:ICategories[]) => {
      categoriesList.push(...res);
    });
    const getTemplatesList = (workflowTemplates:IWorkflowTemplateViewingDto) => {
      viewWorkflowTemplates(workflowTemplates).then((res:ITemplateList) => {
        if(res.content.length <= 0) {
          isShowMore.value = false;
          return;
        }
        templatesList.push(...res.content);
        templatesListCopy.push(...res.content);
      }).finally(() => {
        templateLoading.value = false;
        bottomLoading.value = false;
      });
    };
    getTemplatesList(workflowTemplates);
    
    const searchsTemplate = (name:string) => {
      if(name === ''){
        templatesListCopy.push(...templatesList);
        isShowMore.value = true;
        return;
      }
      templatesListCopy.length = 0;
      isShowMore.value = false;
      templatesList.forEach((item:IContent) => {
        if(item.name.includes(name)){
          templatesListCopy.push(item);
        }
      });
        
    };

    const showMore = () => {
      if (bottomLoading.value) return;
      workflowTemplates.pageNum++;
      bottomLoading.value = true;
      getTemplatesList(workflowTemplates);
    };
    const next = () =>{
      processTemplatesDom.value.validate((valid: boolean) => {
        if(!valid) return false;
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
      search,
      TriggerTypeEnum,
      templatesListCopy,
      templatesList,
      templateLoading,
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
            flex-grow: 1;
            // width: 0;
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
                    ::v-deep(.el-input__inner){
                        border:none;
                    }
                }

                .ptf-r-b-b{
                    padding: 20px 24px 20px 24px;
                    background-color: #fff;
                    // min-height: 736px;
                    &>p{
                        font-size: 14px;
                        color: #082340;
                        margin-bottom:20px;
                    }
                    ul{
                        li{
                            border-radius: 4px;
                            border: 1px solid #B9CFE6;
                            max-height: 360px;
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
                        .show-more{
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

                    
                }
                
            }
        }
    }

}
</style>
