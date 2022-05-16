import yaml from 'yaml';
import { DslTypeEnum, TriggerTypeEnum } from '@/api/dto/enumeration';

export function parse(dsl: string | undefined, triggerType: TriggerTypeEnum | undefined): {
  dslType: DslTypeEnum;
  data: string;
} {
  if (!dsl || !triggerType) {
    return { dslType: DslTypeEnum.PIPELINE, data: '' };
  }

  const { workflow, 'raw-data': data } = yaml.parse(dsl);

  return { dslType: workflow ? DslTypeEnum.WORKFLOW : DslTypeEnum.PIPELINE, data };
}