package dev.jianmu.secret.aggregate;

/**
 * @class: Namespace
 * @description: 命名空间
 * @author: Ethan Liu
 * @create: 2021-04-20 12:36
 **/
public class Namespace {
    private String name;
    private String description;

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
