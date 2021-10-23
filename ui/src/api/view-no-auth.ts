import { restProxy } from '@/api/index';
import { ITaskExecutionRecordVo, ITaskParamVo, IWorkflowExecutionRecordVo } from '@/api/dto/workflow-execution-record';
import { IProjectDetailVo, IProjectQueryingDto, IProjectVo, IProcessTemplate } from '@/api/dto/project';
import { INamespaceDetailVo, INamespaceQueryingDto, INamespaceVo } from '@/api/dto/secret-key';
import { IPageDto, IPageVo } from '@/api/dto/common';
import { INodeVo } from '@/api/dto/node-library';
import { IEventBridgeDetailVo, IEventBridgeQueryingDto, IEventBridgeTargetTransformerVo, IEventBridgeVo, ITargetEventVo } from '@/api/dto/event-bridge';

export const baseUrl = {
  project: '/view/projects',
  workflow: '/view/workflow_instances',
  tasks: '/view/task_instances',
  task: '/view/task_instance',
  log: '/view/logs',
  dsl: '/view/workflow',
  processLog: '/view/logs/workflow',
  secretKey: '/view/namespaces',
  library: '/view/nodes',
  eventBridge: '/view/event_bridges',
  parameter: '/view/parameters',
  targetEvent: '/view/target_events',
  
};
const hubUrl= import.meta.env.VITE_JIANMU_API_BASE_URL;
const baseHubUrl = {
  processTemplate:'/hub/view/workflow_templates',
};
/**
 * 查询项目
 * @param dto
 */
export function queryProject(dto: IProjectQueryingDto): Promise<IProjectVo[]> {
  return restProxy({
    url: baseUrl.project,
    method: 'get',
    payload: dto,
  });
}
/**
 * 获取流程模版
 * @param dto
 */
export function getProcessTemplate(id: number): Promise<IProcessTemplate> {
  return restProxy({
    url:`${hubUrl}${baseHubUrl.processTemplate}/${id}`,
    method: 'get',
  });
}

/**
 * 获取项目详情
 * @param projectId
 */
export function fetchProjectDetail(projectId: string): Promise<IProjectDetailVo> {
  return restProxy({
    url: `${baseUrl.project}/${projectId}`,
    method: 'get',
  });
}

/**
 * 获取流程执行记录列表
 * @param workflowRef
 */
export function listWorkflowExecutionRecord(workflowRef: string): Promise<IWorkflowExecutionRecordVo[]> {
  return restProxy<IWorkflowExecutionRecordVo[]>({
    url: `${baseUrl.workflow}/${workflowRef}`,
    method: 'get',
  });
}

/**
 * 获取任务执行记录列表
 * @param workflowExecutionRecordId
 */
export function listTask(workflowExecutionRecordId: string): Promise<ITaskExecutionRecordVo[]> {
  return restProxy<ITaskExecutionRecordVo[]>({
    url: `${baseUrl.tasks}/${workflowExecutionRecordId}`,
    method: 'get',
  });
}

/**
 * 获取任务参数列表
 * @param taskId
 */
export function listTaskParam(taskId: string): Promise<ITaskParamVo[]> {
  return restProxy<ITaskParamVo[]>({
    url: `${baseUrl.task}/${taskId}/parameters`,
    method: 'get',
  });
}

/**
 * 检查日志
 * @param taskExecutionRecordId
 */
export function checkTaskLog(taskExecutionRecordId: string): Promise<object> {
  return restProxy<object>({
    url: `${baseUrl.log}/${taskExecutionRecordId}`,
    method: 'head',
  });
}

/**
 * 获取日志
 * @param taskExecutionRecordId
 */
export function fetchTaskLog(taskExecutionRecordId: string): Promise<string> {
  return restProxy<string>({
    url: `${baseUrl.log}/${taskExecutionRecordId}`,
    method: 'get',
    timeout: 20 * 1000,
  });
}

/**
 * 检查流程日志
 * @param processExecutionRecordId
 */
export function checkProcessLog(processExecutionRecordId: string): Promise<object> {
  return restProxy<object>({
    url: `${baseUrl.processLog}/${processExecutionRecordId}`,
    method: 'head',
  });
}

/**
 * 获取流程日志
 * @param processExecutionRecordId
 */
export function fetchProcessLog(processExecutionRecordId: string): Promise<string> {
  return restProxy<string>({
    url: `${baseUrl.processLog}/${processExecutionRecordId}`,
    method: 'get',
    timeout: 20 * 1000,
  });
}

/**
 * 获取dsl
 * @param workflowRef
 * @param workflowVersion
 */
export function fetchDsl(workflowRef: string, workflowVersion: string): Promise<string> {
  return restProxy<string>({
    url: `${baseUrl.dsl}/${workflowRef}/${workflowVersion}`,
    method: 'get',
  });
}

/**
 * 查询命名空间
 * @param dto
 */
export function queryNamespace(dto: INamespaceQueryingDto): Promise<IPageVo<INamespaceVo>> {
  return restProxy<IPageVo<INamespaceVo>>({
    url: baseUrl.secretKey,
    method: 'get',
    payload: dto,
  });
}

/**
 * 获取命名空间详情
 * @param name
 */
export function fetchNamespaceDetail(name: string): Promise<INamespaceDetailVo> {
  return restProxy<INamespaceDetailVo>({
    url: `${baseUrl.secretKey}/${name}`,
    method: 'get',
  });
}

/**
 * 获取密钥列表
 * @param namespace
 */
export function listSecretKey(namespace: string): Promise<string[]> {
  return restProxy<string[]>({
    url: `${baseUrl.secretKey}/${namespace}/keys`,
    method: 'get',
  });
}

/**
 * 获取节点库列表
 * @param dto
 */
export function fetchNodeLibraryList(dto: IPageDto): Promise<IPageVo<INodeVo>> {
  return restProxy<IPageVo<INodeVo>>({
    url: `${baseUrl.library}`,
    method: 'get',
    payload: dto,
  });
}

/**
 * 查询事件桥接器
 * @param dto
 */
export function queryEventBridge(dto: IEventBridgeQueryingDto): Promise<IPageVo<IEventBridgeVo>> {
  return restProxy<IPageVo<IEventBridgeVo>>({
    url: `${baseUrl.eventBridge}`,
    method: 'get',
    payload: dto,
  });
}

/**
 * 获取事件桥接器详情
 * @param bridgeId
 */
export function fetchEventBridgeDetail(bridgeId: string): Promise<IEventBridgeDetailVo> {
  return restProxy<IEventBridgeDetailVo>({
    url: `${baseUrl.eventBridge}/${bridgeId}`,
    method: 'get',
  });
}

/**
 * 获取事件桥接器目标转换器模板名称
 */
export function fetchEventBridgeTargetTransformerTemplateName(): Promise<string[]> {
  return restProxy<string[]>({
    url: `${baseUrl.eventBridge}/templates`,
    method: 'get',
    auth: true,
  });
}

/**
 * 获取事件桥接器目标转换器模板
 * @param templateName
 */
export function fetchEventBridgeTargetTransformerTemplate(templateName: string): Promise<IEventBridgeTargetTransformerVo[]> {
  return restProxy<IEventBridgeTargetTransformerVo[]>({
    url: `${baseUrl.eventBridge}/templates/${templateName}`,
    method: 'get',
    auth: true,
  });
}

/**
 * 获取参数类型列表
 */
export function fetchParameterType(): Promise<string[]> {
  return restProxy({
    url: `${baseUrl.parameter}/types`,
    method: 'get',
    auth: true,
  });
}

/**
 * 获取目标事件
 * @param triggerId
 */
export function fetchTargetEvent(triggerId: string): Promise<ITargetEventVo> {
  return restProxy<ITargetEventVo>({
    url: `${baseUrl.targetEvent}/${triggerId}`,
    method: 'get',
  });
}