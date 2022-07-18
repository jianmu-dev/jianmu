package dev.jianmu.oauth2.api.exception;

import dev.jianmu.oauth2.api.enumeration.ErrorCodeEnum;

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
