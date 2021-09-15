package dev.jianmu.hub.intergration.aggregate;

/**
 * @class: NodeLibrary
 * @description: 节点库
 * @author: Ethan Liu
 * @create: 2021-09-15 12:42
 **/
public class NodeLibrary {
    private String id;
    private final String name;
    private final String url;
    private String apiKey;

    public NodeLibrary(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getApiKey() {
        return apiKey;
    }
}
