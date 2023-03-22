import { Cell, Graph } from '@antv/x6';
import yaml from 'yaml';
import { ZoomTypeEnum } from './data/enumeration';
import { NODE } from '../shape/gengral-config';
import { IGlobal, IWorkflow } from './data/common';
import { CustomX6NodeProxy } from './data/custom-x6-node-proxy';
import { DSL_CURRENT_VERSION } from '@/components/workflow/version';
import { IGlobalParam } from './data/common';

const { selectedBorderWidth } = NODE;

const MIN_ZOOM = 20;
const MAX_ZOOM = 500;
// 缩放间隔
const ZOOM_INTERVAL = 10;

// TODO 缺少param的处理
/**
 * 处理cache个数不同时的返回
 */
function buildGlobal(global: IGlobal): {
  concurrent: boolean | number;
  cache: string[] | string | undefined;
  param: IGlobalParam[] | undefined;
} {
  const param =
    !global.params || global.params.length === 0
      ? undefined
      : global.params.map(({ ref, name, value, required, type, hidden }) => ({
        ref,
        name,
        value,
        required,
        type,
        hidden,
      }));

  if (!global.caches || global.caches.length === 0) {
    return {
      concurrent: global.concurrent,
      cache: undefined,
      param,
    };
  }
  if (global.caches.length === 1) {
    return {
      concurrent: global.concurrent,
      cache: global.caches[0],
      param,
    };
  }
  return {
    concurrent: global.concurrent,
    cache: global.caches,
    param,
  };
}

export class WorkflowTool {
  private readonly graph: Graph;

  constructor(graph: Graph) {
    this.graph = graph;
  }

  /**
   * 缩放
   * @param type
   */
  zoom(type: ZoomTypeEnum) {
    // 四舍五入保证与页面上的显示一致
    let factor = Math.round(this.graph.zoom() * 100);
    const remainder = factor % ZOOM_INTERVAL;
    factor -= remainder;

    switch (type) {
      case ZoomTypeEnum.IN:
        factor += ZOOM_INTERVAL;
        factor = factor > MAX_ZOOM ? MAX_ZOOM : factor;
        break;
      case ZoomTypeEnum.OUT:
        factor -= ZOOM_INTERVAL;
        factor = factor < MIN_ZOOM ? MIN_ZOOM : factor;
        if (remainder > 0) {
          factor += ZOOM_INTERVAL;
        }
        break;
      case ZoomTypeEnum.CENTER:
        // 缩放画布内容，使画布(graph)内容充满视口
        this.graph.zoomToFit();

        // 四舍五入保证与页面上的显示一致
        factor = Math.round(this.graph.zoom() * 100);
        if (factor > 100) {
          // 大于100%场景
          factor = 100;
        } else if (factor < MIN_ZOOM) {
          // 小于最小场景
          factor = MIN_ZOOM;
        } else {
          this.optimizeSelectionBoxStyle();
          return;
        }
        break;
      case ZoomTypeEnum.FIT:
        // 缩放画布内容，使画布(graph)内容充满视口
        this.graph.zoomToFit();

        // 四舍五入保证与页面上的显示一致
        factor = Math.round(this.graph.zoom() * 100);
        if (factor < MIN_ZOOM) {
          factor = MIN_ZOOM;
        } else if (factor > MAX_ZOOM) {
          factor = MAX_ZOOM;
        } else {
          this.optimizeSelectionBoxStyle();
          return;
        }
        break;
      case ZoomTypeEnum.ORIGINAL:
        // 将画布内容中心与视口中心对齐
        // this.graph.centerContent();
        factor = 100;
        break;
    }

    const { x, y, width, height } = this.graph.getContentBBox();

    this.graph.zoomTo(factor / 100, {
      center: {
        x: width / 2 + x,
        y: height / 2 + y,
      },
    });

    this.optimizeSelectionBoxStyle();
  }

  optimizeSelectionBoxStyle(): void {
    const factor = this.graph.zoom();

    Array.from(this.graph.container.querySelectorAll<SVGElement>('.x6-widget-transform')).forEach(el => {
      const t = selectedBorderWidth * factor;
      el.style.borderWidth = `${t}px`;
      el.style.marginLeft = `-${t}px`;
      el.style.marginTop = `-${t}px`;
    });
  }

  slimGraphData({ cells }: { cells?: Cell.Properties[] }): void {
    if (!cells) {
      return;
    }

    // 瘦身
    cells.forEach(cell => {
      // 移除所有工具
      delete cell.tools;
      if (cell.shape === 'edge') {
        // 移除边属性
        delete cell.attrs;
        return;
      }
      cell.ports.items.forEach((item: any) => {
        // 移除连接桩属性
        delete item.attrs;
      });
    });
  }

  toDsl(workflowData: IWorkflow): string {
    const triggerNodeProxies: CustomX6NodeProxy[] = [];
    const nodeProxies: CustomX6NodeProxy[] = [];

    this.graph.getNodes().forEach(node => {
      const nodeProxy = new CustomX6NodeProxy(node);
      if (nodeProxy.isTrigger()) {
        triggerNodeProxies.push(nodeProxy);
        return;
      }
      nodeProxies.push(nodeProxy);
    });

    let trigger;
    if (triggerNodeProxies.length > 0) {
      // TODO 待扩展多个触发器
      // eslint-disable-next-line prefer-const
      trigger = triggerNodeProxies[0].toDsl(this.graph);
    }
    const workflow: object[] = [];
    // TODO 优化顺序
    nodeProxies.forEach(nodeProxy => workflow.push(nodeProxy.toDsl(this.graph)));

    let dsl = yaml.stringify({
      version: DSL_CURRENT_VERSION,
      name: workflowData.name,
      description: workflowData.description,
      global: buildGlobal(workflowData.global),
      trigger,
      workflow,
    });

    dsl += '\n\n' + `raw-data: ${JSON.stringify(workflowData.data)}`;

    return dsl;
  }
}
