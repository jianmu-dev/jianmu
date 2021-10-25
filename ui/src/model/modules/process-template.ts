
import {  IWorkflowTemplateViewingDto } from '@/api/dto/process-template';
import { Mutable } from '@/utils/lib';
export interface IWorkflowTemplateViewingForm extends Mutable<IWorkflowTemplateViewingDto>  {

  }

export interface ITemplatesCLickData {
    classifyId?:number;
    templateId?:number;
    templateName:string;
}

export interface IProcessTemplatesForm{
  processTemplatesName:string;
}