import { Graph } from '@antv/x6';
import { ZoomTypeEnum } from './data/enumeration';

const MIN_ZOOM = 0.2;
const MAX_ZOOM = 5;
// 缩放间隔
const ZOOM_INTERVAL = 0.1;

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
        // 缩放画布内容，使画布(graph)内容充满视口
        this.graph.zoomToFit();

        factor = this.graph.zoom();
        if (factor < MIN_ZOOM) {
          factor = MIN_ZOOM;
        } else if (factor > MAX_ZOOM) {
          factor = MAX_ZOOM;
        } else {
          return;
        }
        break;
      case ZoomTypeEnum.ORIGINAL:
        // 将画布内容中心与视口中心对齐
        this.graph.centerContent();
        factor = 1;
        break;
    }

    const { x, y, width, height } = this.graph.getContentBBox();

    this.graph.zoomTo(factor, {
      center: {
        x: width / 2 + x,
        y: height / 2 + y,
      },
    });
  }
}