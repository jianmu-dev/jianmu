import { Graph } from '@antv/x6';
import { ZoomTypeEnum } from '@/components/workflow/workflow-editor/model/enumeration';

const MIN_ZOOM = 0.2;
const MAX_ZOOM = 5;
// 缩放间隔
const ZOOM_INTERVAL = 0.1;

export class WorkflowTool {
  private readonly container: HTMLElement;
  private readonly graph: Graph;

  constructor(graph: Graph) {
    this.container = graph.container;
    this.graph = graph;
  }

  /**
   * 缩放
   * @param type
   */
  zoom(type: ZoomTypeEnum) {
    let factor = this.graph.zoom();

    switch (type) {
      case ZoomTypeEnum.IN:
        factor += ZOOM_INTERVAL;
        factor = factor > MAX_ZOOM ? MAX_ZOOM : factor;
        break;
      case ZoomTypeEnum.OUT:
        factor -= ZOOM_INTERVAL;
        factor = factor < MIN_ZOOM ? MIN_ZOOM : factor;
        break;
      case ZoomTypeEnum.FIT:
        if (this.graph.getNodes().length === 0) {
          factor = 1;
        } else if (this.checkMinContentOverflow()) {
          // 缩放到最小，判断是否溢出
          factor = MIN_ZOOM;
        } else {
          // 缩放画布内容，使画布内容充满视口
          this.graph.zoomToFit();
          return;
        }
        break;
      // case ZoomTypeEnum.ORIGINAL:
      default:
        factor = 1;
        break;
    }

    this.zoomTo(factor);
  }

  /**
   * 缩放
   * @param factor
   * @private
   */
  private zoomTo(factor: number) {
    const currentFactor = this.graph.zoom();

    if (currentFactor === factor) {
      // 缩放比例相同时，忽略
      return;
    }

    const { x, y, width, height } = this.graph.getContentBBox();

    this.graph.zoomTo(factor, {
      center: {
        x: width / 2 + x,
        y: height / 2 + y,
      },
    });

    // 将画布内容中心与视口中心对齐
    this.graph.centerContent();
  }

  /**
   * 检查最小缩放内容是否溢出，相对面板大小
   */
  private checkMinContentOverflow(): boolean {
    const { clientWidth: panelW, clientHeight: panelH } = this.container.parentElement!;
    const { width: contentW, height: contentH } = this.graph.getContentArea();

    const minContentW = contentW * MIN_ZOOM;
    const minContentH = contentH * MIN_ZOOM;

    return minContentW > panelW || minContentH > panelH;
  }
}