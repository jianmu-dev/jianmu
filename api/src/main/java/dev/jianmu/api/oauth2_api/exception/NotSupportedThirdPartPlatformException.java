package dev.jianmu.api.oauth2_api.exception;

import dev.jianmu.api.oauth2_api.enumeration.ErrorCodeEnum;

/**
 * @author huangxi
 * @class NotSupportedThirdPartPlatformException
 * @description NotSupportedThirdPartPlatformException
 * @create 2021-06-30 14:08
 */
public class NotSupportedThirdPartPlatformException extends BaseBusinessException {
    public NotSupportedThirdPartPlatformException() {
        super(ErrorCodeEnum.NOT_SUPPORTED_THIRD_PART_PLATFORM);
    }
}
