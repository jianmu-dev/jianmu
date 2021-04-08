package dev.jianmu.parameter.aggregate;

/**
 * @class: Scope
 * @description: 参数作用域
 * @author: Ethan Liu
 * @create: 2021-02-09 14:23
 **/
public class Scope {
    // 不同情况下是不同的ID
    private String id;
    private Category category;

    private Scope() {
    }

    public String getId() {
        return id;
    }

    public Category getCategory() {
        return category;
    }


    public static final class Builder {
        private String id;
        private Category category;

        private Builder() {
        }

        public static Builder aScope() {
            return new Builder();
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder category(Category category) {
            this.category = category;
            return this;
        }

        public Scope build() {
            Scope scope = new Scope();
            scope.category = this.category;
            scope.id = this.id;
            return scope;
        }
    }
}
