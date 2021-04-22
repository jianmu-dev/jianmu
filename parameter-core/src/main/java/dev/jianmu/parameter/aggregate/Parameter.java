package dev.jianmu.parameter.aggregate;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * @program: Parameter
 * @description: 参数类
 * @author: Ethan Liu
 * @create: 2021-01-21 13:13
 **/
public abstract class Parameter<T> {
    public enum Type {
        STRING {
            @Override
            public Parameter<?> newParameter(Object value) {
                if (value instanceof String) {
                    return new StringParameter((String) value);
                }
                throw new RuntimeException("非法类型");
            }
        },
        BOOL {
            @Override
            public Parameter<?> newParameter(Object value) {
                if (value instanceof Boolean) {
                    return new BoolParameter((Boolean) value);
                }
                throw new RuntimeException("非法类型");
            }
        },
        SECRET {
            @Override
            public Parameter<?> newParameter(Object value) {
                if (value instanceof String) {
                    return new SecretParameter((String) value);
                }
                throw new RuntimeException("非法类型");
            }
        },
        NUMBER {
            @Override
            public Parameter<?> newParameter(Object value) {
                if (value instanceof BigDecimal) {
                    return new NumberParameter((BigDecimal) value);
                }
                throw new RuntimeException("非法类型");
            }
        };

        public abstract Parameter<?> newParameter(Object value);
    }

    // ID
    // TODO 暂时使用UUID的值
    protected final String id = UUID.randomUUID().toString().replace("-", "");
    // 参数类型
    protected Type type;
    // 参数值
    protected final T value;

    protected Parameter(T value) {
        this.value = value;
    }

    public abstract String getStringValue();

    public String getId() {
        return id;
    }

    public Type getType() {
        return type;
    }

    public T getValue() {
        return value;
    }
}
