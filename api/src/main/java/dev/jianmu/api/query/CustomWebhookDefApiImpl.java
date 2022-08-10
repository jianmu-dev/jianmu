package dev.jianmu.api.query;

import dev.jianmu.application.query.CustomWebhookDef;
import dev.jianmu.application.query.CustomWebhookDefApi;
import dev.jianmu.application.service.CustomWebhookDefinitionApplication;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * @author Daihw
 * @class TriggerDefApiImpl
 * @description TriggerDefApiImpl
 * @create 2022/8/9 4:15 下午
 */
@Component
public class CustomWebhookDefApiImpl implements CustomWebhookDefApi {
    private final CustomWebhookDefinitionApplication customWebhookDefinitionApplication;

    public CustomWebhookDefApiImpl(CustomWebhookDefinitionApplication customWebhookDefinitionApplication) {
        this.customWebhookDefinitionApplication = customWebhookDefinitionApplication;
    }

    @Override
    public List<CustomWebhookDef> findByTypes(Set<String> types) {
        return this.customWebhookDefinitionApplication.findByTypes(types);
    }

    @Override
    public List<CustomWebhookDef> getByTypes(Set<String> types) {
        return this.customWebhookDefinitionApplication.findByTypes(types);
    }

    @Override
    public CustomWebhookDef findByType(String type) {
        return this.customWebhookDefinitionApplication.findByType(type);
    }

    @Override
    public CustomWebhookDef getByType(String type) {
        return this.customWebhookDefinitionApplication.findByType(type);
    }

}
