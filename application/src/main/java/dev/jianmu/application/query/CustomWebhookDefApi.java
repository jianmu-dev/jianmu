package dev.jianmu.application.query;

import java.util.List;
import java.util.Set;

/**
 * @author Daihw
 * @class TriggerDefApi
 * @description 触发器定义查询API
 * @create 2022/8/9 4:12 下午
 */
public interface CustomWebhookDefApi {
    List<CustomWebhookDef> findByTypes(Set<String> types);

    List<CustomWebhookDef> getByTypes(Set<String> types);

    CustomWebhookDef findByType(String type);

    CustomWebhookDef getByType(String type);
}
