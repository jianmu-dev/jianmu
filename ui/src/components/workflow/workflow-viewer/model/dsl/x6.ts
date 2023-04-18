import yaml from 'yaml';
import { DslTypeEnum, TriggerTypeEnum } from '@/api/dto/enumeration';

export function parse(
  dsl: string | undefined,
  triggerType: TriggerTypeEnum | undefined,
): {
  dslType: DslTypeEnum;
  asyncTaskRefs: string[];
  data: string;
} {
  if (!dsl || !triggerType) {
    return { dslType: DslTypeEnum.PIPELINE, asyncTaskRefs: [], data: '' };
  }

  const { workflow, pipeline, 'raw-data': rawData } = yaml.parse(dsl);
  return {
    dslType: workflow ? DslTypeEnum.WORKFLOW : DslTypeEnum.PIPELINE,
    asyncTaskRefs: Object.keys(workflow || pipeline),
    data: rawData,
  };
}
