import yaml from 'yaml';
import { BaseGraph } from './base-graph';
import { G6Graph } from './graph/g6';
import { X6Graph } from './graph/x6';
import { TriggerTypeEnum } from '@/api/dto/enumeration';
import { INodeDefVo } from '@/api/dto/project';
import { GraphDirectionEnum } from './data/enumeration';
import { INodeMouseoverEvent } from './data/common';
import { ITaskExecutionRecordVo } from '@/api/dto/workflow-execution-record';
import { Ref } from 'vue';

type ConfigNodeCallbackFnType = (evt: INodeMouseoverEvent) => void
export class WorkflowGraph {
  graph: BaseGraph;
  dsl: string;
  visibleDsl: string;
  isX6: boolean = false;
  nodeInfos: INodeDefVo[];
  triggerType: TriggerTypeEnum;
  private container: HTMLElement;
  private resizeObserver: ResizeObserver;
  private configNodeCallbackFn: ConfigNodeCallbackFnType;

  constructor(dsl: string, nodeInfos: INodeDefVo[], triggerType: TriggerTypeEnum, container: HTMLElement, configNodeCallbackFn: ConfigNodeCallbackFnType) {
    this.dsl = dsl;
    this.nodeInfos = nodeInfos;
    this.triggerType = triggerType;
    this.container = container;
    this.configNodeCallbackFn = configNodeCallbackFn;
    const data = yaml.parse(dsl);
    if (data['raw-data']) {
      delete data['raw-data'];
      this.isX6 = true;
      this.visibleDsl = yaml.stringify(data);
    } else {
      this.isX6 = false;
      this.visibleDsl = yaml.stringify(data);
    }
    this.graph = this.isX6? new X6Graph(dsl, triggerType, this.container) : new G6Graph(dsl, triggerType, nodeInfos, this.container, GraphDirectionEnum.HORIZONTAL);
    this.graph.configNodeAction(this.configNodeCallbackFn);
    this.resizeObserver = new ResizeObserver(() => {
      const { clientWidth, clientHeight } = this.container.parentElement!;
      this.graph.changeSize(clientWidth, clientHeight);
    });
    this.resizeObserver.observe(this.container.parentElement!);
  }
  // 获取当前graph的旋转对应方向
  getRotationDirection() {
    return this.graph.getDirection() === GraphDirectionEnum.HORIZONTAL ? GraphDirectionEnum.VERTICAL : GraphDirectionEnum.HORIZONTAL;
  }
  // 旋转
  rotation(tasks: ITaskExecutionRecordVo[]) {
    // 销毁旧画布
    this.destroy();
    this.graph = new G6Graph(this.dsl, this.triggerType, this.nodeInfos, this.container, this.getRotationDirection());
    this.graph.configNodeAction(this.configNodeCallbackFn);
    // 点亮状态
    this.graph.updateNodeStates(tasks);
    this.resizeObserver = new ResizeObserver(() => {
      const { clientWidth, clientHeight } = this.container.parentElement!;
      this.graph.changeSize(clientWidth, clientHeight);
    });
    this.resizeObserver.observe(this.container.parentElement!);
  }
  // 更新zoom缩放层级
  getZoom(callBack: (n:number) => void) {
    callBack(Math.round(this.graph.getZoom() * 100));
  }
  // 更新zoom缩放层级s
  getZooms(zoom: Ref<number>) {
    zoom.value = Math.round(this.graph.getZoom() * 100);
  }
  // 更新是否X6
  getIsx6(isX6: Ref<boolean>) {
    isX6.value = this.isX6;
  }
  // 缩放
  handleZoom(zoom: Ref<number>, val?: number){
    // undefined 适配
    if (val === undefined) {
      this.graph.fitView();
    } else {
      this.graph.zoomTo(val);
    }
    this.getZooms(zoom);
  }
  // 全屏/退出全屏
  handleFullscreen(zoom: Ref<number>) {
    this.container.style.visibility = 'hidden';
    setTimeout(() => {
      this.graph.fitCanvas();
      this.getZooms(zoom);
      this.container.style.visibility = '';
    }, 50);
  }
  // 更新节点状态
  updateNodeStates(tasks: ITaskExecutionRecordVo[]) {
    // TODO
    setTimeout(() => {
      this.graph.updateNodeStates(tasks);
    }, 100);
  }
  // 高亮节点方法
  highlightNodeState(status: string, boo: boolean) {
    this.graph.highlightNodeState(status, boo);
  }
  // 隐藏节点工具栏
  hideNodeToolbar(nodeId: string) {
    this.graph.hideNodeToolbar(nodeId);
  }
  // 获取异步任务数量
  getAsyncTaskNodeCount() {
    return this.graph.getAsyncTaskNodeCount();
  }
  getGraphType() {
    return this.graph.getGraphType();
  }
  getDslType() {
    return this.graph.dslType;
  }
  getGraph() {
    return this.graph;
  }
  destroy() {
    // 销毁监控容器大小变化
    this.resizeObserver && this.resizeObserver.disconnect();
    // 销毁画布
    this.container.innerHTML = '';
    console.log('container remove');
  }
}