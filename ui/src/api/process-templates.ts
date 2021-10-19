import { restProxy } from '@/api';
import { ICategories, IWorkflowTemplateViewingDto, ITemplateList } from '@/api/dto/process-templates';

export const baseUrl = import.meta.env.VITE_JIANMU_API_BASE_URL;

/**
 * 查看流程模版分类列表
 * @param dto
 */
export function workflowTemplatesCategories(): Promise<ICategories[]> {
  return restProxy<ICategories[]>({
    url: `${baseUrl}/hub/view/workflow_templates/categories`,
    method: 'get',
    auth: true,
  });
}

/**
 * 查看流程列表
 * @param dto
 */
export function viewWorkflowTemplates(dto:IWorkflowTemplateViewingDto): Promise<ITemplateList> {
  return restProxy<ITemplateList>({
    url: `${baseUrl}/hub/view/workflow_templates`,
    method: 'get',
    auth: true,
    payload: dto,
  });
}

