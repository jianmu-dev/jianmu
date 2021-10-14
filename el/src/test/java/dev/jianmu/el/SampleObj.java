package dev.jianmu.el;


import dev.jianmu.workflow.el.ElVariable;

/**
 * @class: SampleObj
 * @description: TODO
 * @author: Ethan Liu
 * @create: 2021-02-21 11:01
 **/
public class SampleObj implements ElVariable {
    private String name;
    private int age;

    public SampleObj(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
