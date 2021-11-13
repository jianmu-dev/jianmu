import { EdgeConfig, NodeConfig } from '@antv/g6';
import yaml from 'yaml';
import { NodeTypeEnum } from '../utils/enumeration';
import { TriggerTypeEnum } from '@/api/dto/enumeration';
import { INodeDefVo } from '@/api/dto/project';
import shellIcon from '../svgs/shape/shell.svg';
import { SHELL_NODE_TYPE } from './model';

/**
 * 节点标签最长长度
 */
export const MAX_LABEL_LENGTH = 10;

/**
 * 解析webhook节点
 * @param nodes
 * @param edges
 * @param isWorkflow
 */
function parseWebhook(nodes: NodeConfig[], edges: EdgeConfig[], isWorkflow: boolean): void {
  const key = 'webhook';
  const label = key;
  const type = NodeTypeEnum.WEBHOOK;

  nodes.push({
    id: key,
    label,
    description: key,
    type,
  });

  let startNode;

  if (isWorkflow) {
    startNode = nodes.find(item => item.type === NodeTypeEnum.START) as NodeConfig;
  } else {
    startNode = nodes[0];
  }

  edges.push({
    source: key,
    target: startNode.id,
    type: 'flow',
  });
}

/**
 * 解析cron节点
 * @param cron
 * @param nodes
 * @param edges
 * @param isWorkflow
 */
function parseCron(cron: string | undefined, nodes: NodeConfig[], edges: EdgeConfig[], isWorkflow: boolean): void {
  if (!cron) {
    // 不存在时，忽略
    return;
  }

  const key = NodeTypeEnum.CRON;
  // const label = cron.length > MAX_LABEL_LENGTH ? `${cron.substr(0, MAX_LABEL_LENGTH)}...` : cron;
  const label = 'cron';
  const description = cron;
  const type = NodeTypeEnum.CRON;

  nodes.push({
    id: key,
    label,
    description,
    type,
  });

  let startNode;

  if (isWorkflow) {
    startNode = nodes.find(item => item.type === NodeTypeEnum.START) as NodeConfig;
  } else {
    startNode = nodes[0];
  }

  edges.push({
    source: key,
    target: startNode.id,
    type: 'flow',
  });
}

/**
 * 解析workflow节点
 * @param workflow
 */
function parseWorkflow(workflow: any): {
  nodes: NodeConfig[];
  edges: EdgeConfig[];
} {
  const nodes: NodeConfig[] = [];
  const edges: EdgeConfig[] = [];

  Object.keys(workflow).forEach(key => {
    if (typeof workflow[key] !== 'object') {
      // 非对象表示非节点，忽略
      return;
    }

    const { type: nodeType } = workflow[key];

    const label = key.length > MAX_LABEL_LENGTH ? `${key.substr(0, MAX_LABEL_LENGTH)}...` : key;
    let description = key;
    let type = nodeType;
    let uniqueKey;
    switch (nodeType) {
      case NodeTypeEnum.START:
      case NodeTypeEnum.END:
        break;
      case NodeTypeEnum.CONDITION:
        description += `<br/>${workflow[key].expression}`;
        break;
      default: {
        type = NodeTypeEnum.ASYNC_TASK;
        const { image } = workflow[key];
        uniqueKey = image ? SHELL_NODE_TYPE : workflow[key].type;
        break;
      }
    }

    nodes.push({
      id: key,
      label,
      description,
      type,
      uniqueKey,
    });

    const { sources, targets } = workflow[key];

    sources?.forEach((source: string) => {
      if (edges.find(item => item.source === source && item.target === key)) {
        // 已存在时，忽略
        return;
      }

      edges.push({
        source,
        target: key,
        type: 'flow',
      });
    });

    targets?.forEach((target: string) => {
      if (edges.find(item => item.source === key && item.target === target)) {
        // 已存在时，忽略
        return;
      }

      edges.push({
        source: key,
        target,
        type: 'flow',
      });
    });
  });

  edges.forEach(edge => {
    const { type, cases } = workflow[edge.source as string];
    if (type === NodeTypeEnum.CONDITION) {
      // 设置从条件网关出来的边内容
      for (const k of Object.keys(cases)) {
        const v = cases[k];

        if (v === edge.target) {
          edge.label = k;

          break;
        }
      }
    }
  });

  return { nodes, edges };
}

/**
 * 解析pipeline节点
 * @param pipeline
 */
function parsePipeline(pipeline: any): {
  nodes: NodeConfig[];
  edges: EdgeConfig[];
} {
  const nodes: NodeConfig[] = [];
  const edges: EdgeConfig[] = [];

  Object.keys(pipeline).forEach(key => {
    if (typeof pipeline[key] !== 'object') {
      // 非对象表示非节点，忽略
      return;
    }

    if (nodes.length > 0) {
      edges.push({
        source: nodes[nodes.length - 1].id,
        target: key,
        type: 'flow',
      });
    }

    const label = key.length > MAX_LABEL_LENGTH ? `${key.substr(0, MAX_LABEL_LENGTH)}...` : key;
    const { image, type } = pipeline[key];

    nodes.push({
      id: key,
      label,
      description: key,
      type: NodeTypeEnum.ASYNC_TASK,
      uniqueKey: image ? SHELL_NODE_TYPE : type,
    });
  });

  return { nodes, edges };
}

/**
 * 解析yaml
 * @param dsl
 * @param triggerType
 * @param nodeInfos
 */
export function parse(dsl: string | undefined, triggerType: TriggerTypeEnum | undefined, nodeInfos?: INodeDefVo[]): {
  nodes: NodeConfig[];
  edges: EdgeConfig[];
} {
  if (!dsl || !triggerType) {
    return { nodes: [], edges: [] };
  }

  const { trigger, workflow, pipeline } = yaml.parse(dsl);

  // 解析workflow节点
  const { nodes, edges } = workflow ? parseWorkflow(workflow) : parsePipeline(pipeline);

  switch (triggerType) {
    case TriggerTypeEnum.CRON:
      // 解析cron节点
      parseCron(trigger.schedule, nodes, edges, !!workflow);
      break;
    case TriggerTypeEnum.WEBHOOK:
      // 解析webhook节点
      parseWebhook(nodes, edges, !!workflow);
      break;
  }

  if (nodeInfos) {
    // 匹配icon
    nodes.forEach((node: NodeConfig) => {
      if (node.uniqueKey === SHELL_NODE_TYPE) {
        node.iconUrl = shellIcon;
        return;
      }
      node.iconUrl = nodeInfos.find(nodeInfo => nodeInfo.type === node.uniqueKey)?.icon;
    });
  }

  return { nodes, edges };
}