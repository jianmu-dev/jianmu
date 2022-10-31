import { Mutable } from '@/utils/lib';
import { IGitRepoVo } from '@/api/dto/git-repo';

/**
 * 创建密钥表单
 */
export interface IGitRepo extends Mutable<IGitRepoVo> {}
