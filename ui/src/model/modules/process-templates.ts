
import {  IWorkflowTemplateViewingDto } from '@/api/dto/process-templates';
import { Mutable } from '@/utils/lib';
export interface IWorkflowTemplateViewingForm extends Mutable<IWorkflowTemplateViewingDto>  {

  }


export interface templatesCLickData {
    classifyId:number|undefined; 
    templateId:number|undefined;
    templateName:string
}