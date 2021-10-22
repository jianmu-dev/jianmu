<template>
    <div class="process-templates">
        <div class="right-top-btn">
            <router-link :to="{name: 'index'}">
                <jm-button class="jm-icon-button-cancel" size="small">取消</jm-button>
            </router-link>
            
            <jm-button @click="next" type="primary" class="jm-icon-button-next" size="small">下一步</jm-button>
           
        </div>
        <div  class="process-templates-flex">
            <!-- left -->
            <div v-if="categoriesList.length > 0 " class="ptf-l">
                <div class="ptf-l-t">
                    <i></i>
                    <span>流程模版</span>
                </div>

                <div  class="ptf-l-b">
                    <p>分类</p>
                    <jm-scrollbar max-height="50vh">
                        <ul>
                            <li v-for="i in categoriesList"
                                :key="i.id"
                                :class="{click:templatesCLickData.classifyId === i.id}"
                                @click="classifyClic(i)"
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
                        <jm-input  @change="searchsTemplate"  v-model="workflowTemplates.name" placeholder="请输入模版名称">
                            <template #prefix>
                                <i class="el-input__icon el-icon-search"></i>
                            </template>
                        </jm-input>
                    </div>
                    
                    <div  class="ptf-r-b-b">
                        <jm-empty v-if="templatesListCopy.length <= 0 && !templateLoading" />
                        <template v-else>
                            <p>选择流程模版</p>
                            <ul v-loading="templateLoading">
                                <li v-for="item in templatesListCopy"
                                    :key="item.id"
                                    :class="{click:templatesCLickData.templateId === item.id}"
                                    @click="templatesItem(item)"
                                    >
                                    <div class="templates-tit">
                                        <h3 :style="{backgroundColor:'#' + ('00000' + (('0.'+item.id) * 0x1000000 << 0).toString(16)).substr(-6)}"
                                            class="templates-tit-h3">
                                            {{ item.type[0] }}
                                            </h3>
                                        <p>{{ item.name }}</p>
                                    </div>
                                    <jm-workflow-viewer style="height: 360px;"
                                                        :dsl="item.dsl"
                                                        readonly
                                                        :node-infos="item.nodeDefs"
                                                        :trigger-type="TriggerTypeEnum.MANUAL"
                                                    />
                                </li>
                                <p v-if="isShowMore" @click="showMore" class="show-more">
                                    <span>显示更多</span>
                                    <i class="btm-down" :class="{'btn-loading':bottomLoading}"></i>
                                </p>
                            </ul>
                        </template>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, reactive, ref } from 'vue';
import { workflowTemplatesCategories, viewWorkflowTemplates } from '@/api/process-templates';
import { ICategories, ITemplateListVo, IContentVo } from '@/api/dto/process-templates';
import { TriggerTypeEnum } from '@/api/dto/enumeration';
import { IWorkflowTemplateViewingForm, templatesCLickData }from '@/model/modules/process-templates';
import router from '@/router';
import { useRoute } from 'vue-router';
export default defineComponent({
  setup() {
    const route = useRoute();
    const categoriesList = reactive<ICategories[]>([]);
    const processTemplatesForm = reactive<{ name:string }>({
      name:route.query.processTemplatesName as string || '',
    });
    const workflowTemplates = reactive<IWorkflowTemplateViewingForm>({
      pageNum: 1,
      pageSize: 10,
      name: '',
      templateCategoryId: undefined,
    });
    const templatesList:IContentVo[] = [];
    const templatesListCopy = reactive<IContentVo[]>([]);
    const rules = {
      name:[
        { 
          required: true,
          message: '请输入项目名称',
          trigger: 'change',
        },
      ],
    };
    const templatesCLickData = reactive<templatesCLickData>({
      templateId:undefined,
      templateName:'',
      classifyId:undefined,
    });
    const isShowMore = ref<boolean>(false);
    const templateLoading = ref<boolean>(true);
    const bottomLoading = ref<boolean>(false);
    const processTemplatesDom = ref<any>(null);
    let classifyClickKey = true;
    // 流程模版
    const getTemplatesList = (workflowTemplates:IWorkflowTemplateViewingForm) => {
      templateLoading.value = true;
      isShowMore.value = false;
      viewWorkflowTemplates(workflowTemplates).then((res:ITemplateListVo) => {
        if(res.content.length <= 0) {
          isShowMore.value = false;
          return;
        }
        isShowMore.value = true;
        templatesList.push(...res.content);
        templatesListCopy.push(...res.content);
      }).finally(() => {
        templateLoading.value = false;
        bottomLoading.value = false;
        classifyClickKey = true;
      });
    };
    // 列表
    workflowTemplatesCategories().then((res:ICategories[]) => {
      categoriesList.push(...res);
      getTemplatesList(workflowTemplates);
    }).finally(() => {
      templateLoading.value = false;
    });
    
    
    const searchsTemplate = (name:string) => {
      templatesListCopy.length = 0;
      if(name === ''){
        templatesListCopy.push(...templatesList);
        isShowMore.value = true;
        return;
      }
      isShowMore.value = false;
      templatesList.forEach((item:IContentVo) => {
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
        router.push({ 
          name:'create-workflow-definition',
          query:{
            processTemplatesName:processTemplatesForm.name,
            processTemplatesId:templatesCLickData.templateId,
            source:'processTemplates',
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
      templatesListCopy,
      templatesList,
      templateLoading,
      workflowTemplates,
      classifyClic(i:ICategories){
        if(!classifyClickKey) return; 
        classifyClickKey = false;
        if(i.id === templatesCLickData.classifyId){
          templatesCLickData.classifyId = undefined;
          workflowTemplates.templateCategoryId = undefined;
        }else{
          templatesCLickData.classifyId = i.id;
          workflowTemplates.templateCategoryId = i.id;
        }
        templatesList.length = 0;
        templatesListCopy.length = 0;
        
        isShowMore.value = true;
        workflowTemplates.pageNum = 1;
        workflowTemplates.name = '';
        getTemplatesList(workflowTemplates);
      },
      templatesCLickData,
      templatesItem(item:IContentVo){
        if(templatesCLickData.templateId === item.id){
          templatesCLickData.templateId = undefined;
          templatesCLickData.templateName = '';
          if(processTemplatesForm.name === item.name){
            processTemplatesForm.name = '';
          }
        }else{
          templatesCLickData.templateId = item.id;
          // 和上一次不一样就说明修改过
          if(processTemplatesForm.name === templatesCLickData.templateName || processTemplatesForm.name === ''){
            processTemplatesForm.name = item.name;
          }
          templatesCLickData.templateName = item.name;
        }
      },
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
                    .click{
                        color: #096DD9;
                        cursor: pointer;
                    }
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
                    min-height: 500px;
                    &>p{
                        font-size: 14px;
                        color: #082340;
                        margin-bottom:20px;
                    }
                    ul{
                        min-height: 500px;
                        .click{
                            border: 1px solid #096DD9;
                            box-shadow: 0px 0px 16px 4px #DCE3EF;
                        }
                        li{
                            border-radius: 4px;
                            border: 1px solid #B9CFE6;
                            max-height: 360px;
                            position: relative;
                            margin-bottom:20px;
                            
                            &:hover{
                                border: 1px solid #096DD9;
                                box-shadow: 0px 0px 16px 4px #DCE3EF;
                            }
                            .templates-tit{
                                position: absolute;
                                top: 20px;
                                left: 20px;
                                font-size: 16px;
                                color: #3F536E;
                                display: flex;
                                align-items: center;
                                .templates-tit-h3{
                                    border-radius: 50%;
                                    width: 30px;
                                    height: 30px;
                                    display: flex;
                                    justify-content: center;
                                    align-items: center;
                                    color: #fff;
                                    margin-right: 14px;;
                                    font-size: 17px;
                                }
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
