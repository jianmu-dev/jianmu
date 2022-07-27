package dev.jianmu.oauth2.api.exception;

import dev.jianmu.oauth2.api.enumeration.ErrorCodeEnum;

/**
 * @author huangxi
 * @class RepoExistedException
 * @description RepoExistedException
 * @create 2022-07-27 17:43
 */
public class RepoExistedException extends BaseBusinessException{
    public RepoExistedException(String msg) {
        super(ErrorCodeEnum.REPO_NOT_EXISTED, msg);
    }
}
