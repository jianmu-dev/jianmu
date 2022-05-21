import { Cell } from '@antv/x6';
import yaml from 'yaml';
import { DslTypeEnum, TriggerTypeEnum } from '@/api/dto/enumeration';
import { NodeTypeEnum } from '@/components/workflow/workflow-editor/model/data/enumeration';

export function parse(dsl: string | undefined, triggerType: TriggerTypeEnum | undefined): {
  dslType: DslTypeEnum;
  asyncTaskRefs: string[];
  data: string;
} {
  if (!dsl || !triggerType) {
    return { dslType: DslTypeEnum.PIPELINE, asyncTaskRefs: [], data: '' };
  }

  const { trigger, workflow, pipeline, 'raw-data': rawData } = yaml.parse(dsl);

  let data: string;
  if (triggerType === TriggerTypeEnum.MANUAL && trigger) {
    let triggerNodeId: string | undefined;
    const cells = (JSON.parse(rawData).cells as Cell.Properties[])
      // 过滤触发器
      .filter(cell => {
        if (cell.shape !== 'vue-shape') {
          return true;
        }
        const { type } = JSON.parse(cell.data);
        if ([NodeTypeEnum.CRON, NodeTypeEnum.WEBHOOK].includes(type)) {
          triggerNodeId = cell.id;
          return false;
        }
        return true;
      })
      // 过滤触发器相关边
      .filter(cell => {
        if (cell.shape !== 'edge') {
          return true;
        }
        return triggerNodeId !== cell.source.cell;
      });
    data = JSON.stringify({ cells });
  } else {
    data = rawData;
  }

  return {
    dslType: workflow ? DslTypeEnum.WORKFLOW : DslTypeEnum.PIPELINE,
    asyncTaskRefs: Object.keys(workflow || pipeline),
    data,
  };
}