import {IExternalParameterCreatingDto, IExternalParameterUpdatingDto, IExternalParameterVo,IExternalParameterLabelVo} from './dto/ext-param'
import {restProxy} from "@/api/index";

const getExtParamUrl = '/view/external_parameters';
const getExtParamLabelUrl = '/view/external_parameters/labels'
const extParamUrl = '/external_parameters';

/**
 * 获取外部参数列表
 */
export function getExtParamList(): Promise<IExternalParameterVo[]> {
  return restProxy({
    url: `${getExtParamUrl}`,
    method: 'get',
  });
}

/**
 * 创建外部参数
 * @param dto
 */
export function addExtParam(dto: IExternalParameterCreatingDto): Promise<void> {
  return restProxy({
    url: `${extParamUrl}`,
    method: 'post',
    payload: dto,
    auth: true,
  });
}

/**
 * 删除外部参数
 */
export function deleteExtParam(id: string): Promise<void> {
  return restProxy({
    url: `${extParamUrl}/${id}`,
    method: 'delete',
    auth: true,
  });
}

/**
 * 编辑外部参数
 * @param dto
 */
export function editorExtParam(dto: IExternalParameterUpdatingDto): Promise<void> {
  return restProxy({
    url: `${extParamUrl}`,
    method: 'put',
    payload: dto,
    auth: true,
  });
}

export function getExtParamLabelList():Promise<IExternalParameterLabelVo[]>{
  return restProxy({
    url:`${getExtParamLabelUrl}`,
    method:'get',
  });
}