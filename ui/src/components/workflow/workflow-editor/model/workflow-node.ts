import { IWorkflowNode } from './data/common';
import { Cron } from './data/node/cron';
import { Webhook } from './data/node/webhook';
import { Shell } from './data/node/shell';
import { AsyncTask } from './data/node/async-task';
import { fetchNodeLibraryList, getOfficialNodes } from '@/api/view-no-auth';
import { NodeTypeEnum } from '@/api/dto/enumeration';

interface IPageInfo {
  pageNum: number;
  totalPages: number;
  content: IWorkflowNode[];
}

export class WorkflowNode {

  constructor() {
  }

  loadInnerTriggers(keyword?: string): IWorkflowNode[] {
    const arr: IWorkflowNode[] = [new Cron(), new Webhook()];

    return keyword ? arr.filter(item => item.getName().includes(keyword)) : arr;
  }

  loadInnerNodes(keyword?: string): IWorkflowNode[] {
    const arr: IWorkflowNode[] = [new Shell()];

    return keyword ? arr.filter(item => item.getName().includes(keyword)) : arr;

  }

  async loadLocalNodes(pageNum: number, pageSize: number, keyword?: string): Promise<IPageInfo> {
    const { list, pageNum: currentPageNum, pages: totalPages } = await fetchNodeLibraryList({
      pageNum,
      pageSize,
      type: NodeTypeEnum.LOCAL,
      name: keyword,
    });
    const arr: IWorkflowNode[] = list.map(item => new AsyncTask(item.ownerRef, item.ref, item.name, item.icon));
    return {
      pageNum: currentPageNum,
      totalPages,
      content: keyword ? arr.filter(item => item.getName().includes(keyword)) : arr,
    };
  }

  async loadOfficialNodes(pageNum: number, pageSize: number, keyword?: string): Promise<IPageInfo> {
    const { content, pageNum: currentPageNum, totalPages } = await getOfficialNodes({
      pageNum,
      pageSize,
      name: keyword,
    });
    const arr: IWorkflowNode[] = content.map(item => new AsyncTask(item.ownerRef, item.ref, item.name, item.icon));
    return {
      pageNum: currentPageNum,
      totalPages,
      content: keyword ? arr.filter(item => item.getName().includes(keyword)) : arr,
    };
  }

  async loadCommunityNodes(pageNum: number, pageSize: number, keyword?: string): Promise<IPageInfo> {
    const { content, pageNum: currentPageNum, totalPages } = await getOfficialNodes({
      pageNum,
      pageSize,
      name: keyword,
      type: NodeTypeEnum.COMMUNITY,
    });
    const arr: IWorkflowNode[] = content.map(item => new AsyncTask(item.ownerRef, item.ref, item.name, item.icon));
    return {
      pageNum: currentPageNum,
      totalPages,
      content: keyword ? arr.filter(item => item.getName().includes(keyword)) : arr,
    };
  }
}
