import { IWorkflowNode } from './data/common';
import { Cron } from './data/node/cron';
import { Webhook } from './data/node/webhook';
import { Shell } from './data/node/shell';
import { AsyncTask } from './data/node/async-task';
import { fetchNodeLibraryList, getOfficialNodes } from '@/api/view-no-auth';
import { NodeCategoryEnum } from '@/api/dto/enumeration';
import { INodeParameterVo } from '@/api/dto/node-definitions';
import { ParamTypeEnum } from '@/components/workflow/workflow-editor/model/data/enumeration';
import { Start } from './data/node/start';
import { End } from './data/node/end';
import { CustomWebhook } from './data/node/custom-webhook';
import { IEventVo, IWebhookDefinitionVo } from '@/api/dto/custom-webhook';
import { getWebhookList } from '@/api/custom-webhook';
import { v4 as uuidv4 } from 'uuid';

interface IPageInfo {
  pageNum: number;
  totalPages: number;
  content: IWorkflowNode[];
}

/**
 * push输入/输出参数
 * @param data 原数据
 * @param inputs
 * @param outputs
 * @param versionDescription
 */
export const pushParams = (
  data: AsyncTask,
  inputs: INodeParameterVo[],
  outputs: INodeParameterVo[],
  versionDescription: string,
) => {
  data.versionDescription = versionDescription;
  if (inputs) {
    inputs.forEach(item => {
      const type = item.type as ParamTypeEnum;
      let value = (item.value || '').toString();
      if (type === ParamTypeEnum.STRING) {
        value = JSON.stringify(value);
      }
      data.inputs.push({
        ref: item.ref,
        name: item.name,
        type,
        value,
        required: item.required,
        description: item.description,
      });
    });
  }
  if (outputs) {
    outputs.forEach(item => {
      data.outputs.push({
        ref: item.ref,
        name: item.name,
        type: item.type as ParamTypeEnum,
        value: (item.value || '').toString(),
        required: item.required,
        description: item.description,
      });
    });
  }
};

/**
 * push自定义触发事件
 */
export const pushCustomEvents = (data: CustomWebhook, events: IEventVo[], version: string, dslText: string) => {
  data.version = version;
  data.dslText = dslText;
  events.forEach(item => {
    const availableParams = item.availableParams.map(param => ({
      key: uuidv4(),
      ...param,
    }));
    data.events.push({
      ref: item.ref,
      name: item.name,
      availableParams,
      eventRuleset: item.ruleset,
    });
  });
};

async function getWebhookLists(): Promise<IWebhookDefinitionVo[]> {
  return await getWebhookList();
}

export class WorkflowNode {
  private readonly entry: boolean;

  constructor(entry: boolean) {
    this.entry = entry;
  }

  async loadInnerTriggers(keyword?: string): Promise<IWorkflowNode[]> {
    const arr: IWorkflowNode[] = [new Cron()];
    if (this.entry) {
      // 获取webhook列表
      const customWebhookList = await getWebhookLists();
      // 动态构建custom-webhook部分
      customWebhookList.forEach(item => {
        arr.push(new CustomWebhook(item.ref, item.ref, item.name, item.icon, item.ownerRef));
      });
    } else {
      arr.push(new Webhook());
    }
    return keyword ? arr.filter(item => item.getName().toLowerCase().includes(keyword.toLowerCase())) : arr;
  }

  loadInnerNodes(keyword?: string): IWorkflowNode[] {
    const arr: IWorkflowNode[] = [new Shell(), new Start(), new End()];

    return keyword ? arr.filter(item => item.getName().toLowerCase().includes(keyword.toLowerCase())) : arr;
  }

  async loadLocalNodes(pageNum: number, pageSize: number, keyword?: string): Promise<IPageInfo> {
    const {
      list,
      pageNum: currentPageNum,
      pages: totalPages,
    } = await fetchNodeLibraryList({
      pageNum,
      pageSize,
      type: NodeCategoryEnum.LOCAL,
      name: keyword,
    });
    const arr: IWorkflowNode[] = list.map(
      item => new AsyncTask(item.ownerRef, item.ref, item.ref, item.name, item.icon),
    );
    return {
      pageNum: currentPageNum,
      totalPages,
      content: arr,
    };
  }

  async loadOfficialNodes(pageNum: number, pageSize: number, keyword?: string): Promise<IPageInfo> {
    const {
      content,
      pageNum: currentPageNum,
      totalPages,
    } = await getOfficialNodes({
      pageNum,
      pageSize,
      name: keyword,
    });
    const arr: IWorkflowNode[] = content.map(
      item => new AsyncTask(item.ownerRef, item.ref, item.ref, item.name, item.icon),
    );
    return {
      pageNum: currentPageNum,
      totalPages,
      content: arr,
    };
  }

  async loadCommunityNodes(pageNum: number, pageSize: number, keyword?: string): Promise<IPageInfo> {
    const {
      content,
      pageNum: currentPageNum,
      totalPages,
    } = await getOfficialNodes({
      pageNum,
      pageSize,
      name: keyword,
      type: NodeCategoryEnum.COMMUNITY,
    });
    const arr: IWorkflowNode[] = content.map(
      item => new AsyncTask(item.ownerRef, item.ref, item.ref, item.name, item.icon),
    );
    return {
      pageNum: currentPageNum,
      totalPages,
      content: arr,
    };
  }
}
