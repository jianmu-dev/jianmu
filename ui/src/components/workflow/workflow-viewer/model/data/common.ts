import { NodeTypeEnum } from './enumeration';

// shell节点类型
export const SHELL_NODE_TYPE = 'shell';

/**
 * 节点鼠标滑过事件
 */
export interface INodeMouseoverEvent extends Readonly<{
  /**
   * 节点id
   */
  id: string;
  /**
   * 节点描述
   */
  description: string;
  /**
   * 节点类型
   */
  type: NodeTypeEnum;
  /**
   * 节点宽度
   */
  width: number;
  /**
   * 节点高度
   */
  height: number;
  /**
   * 节点中心坐标x轴
   */
  x: number;
  /**
   * 节点中心坐标y轴
   */
  y: number;
}> {
}