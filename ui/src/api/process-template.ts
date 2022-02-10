import { restProxy } from '@/api';
import { ICategoriesVo, IWorkflowTemplateViewingDto, ITemplateListVo } from '@/api/dto/process-template';

export const baseUrl = import.meta.env.VITE_JIANMU_HUB_API_BASE_URL;

/**
 * 查看流程模版分类列表
 */
export function workflowTemplateCategories(): Promise<ICategoriesVo[]> {
  return restProxy<ICategoriesVo[]>({
    url: `${baseUrl}/hub/view/workflow_templates/categories`,
    method: 'get',
  });
}

/**
 * 查看流程列表
 * @param dto
 */
export function viewWorkflowTemplate(dto:IWorkflowTemplateViewingDto): Promise<ITemplateListVo> {
  return restProxy<ITemplateListVo>({
    url: `${baseUrl}/hub/view/workflow_templates`,
    method: 'get',
    payload: dto,
  });
}

