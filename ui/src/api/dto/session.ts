/**
 * 创建会话dto
 */
export interface ISessionCreatingDto extends Readonly<{
  username: string;
  password: string;
}> {
}

/**
 * 会话值对象
 */
export interface ISessionVo extends Readonly<{
  token: string;
  type: string;
  id: number;
  username: string;
}> {
}