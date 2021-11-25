package dev.jianmu.workflow.aggregate.parameter;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

/**
 * @program: Parameter
 * @description 参数类
 * @author Ethan Liu
 * @create 2021-01-21 13:13
*/
public abstract class Parameter<T> {
    public enum Type {
        STRING {
            @Override
            public Parameter<?> newParameter(Object value) {
                if (value == null) {
                    return defaultParameter();
                }
                if (value instanceof String) {
                    return new StringParameter((String) value);
                }
                throw new ClassCastException("参数值与类型不匹配，无法转换");
            }

            @Override
            public Parameter<?> defaultParameter() {
                return new StringParameter("");
            }
        },
        BOOL {
            @Override
            public Parameter<?> newParameter(Object value) {
                if (value == null) {
                    return defaultParameter();
                }
                if (value instanceof Boolean) {
                    return new BoolParameter((Boolean) value);
                }
                throw new ClassCastException("参数值与类型不匹配，无法转换");
            }

            @Override
            public Parameter<?> defaultParameter() {
                return new BoolParameter(false);
            }
        },
        SECRET {
            @Override
            public Parameter<?> newParameter(Object value) {
                if (value == null) {
                    return defaultParameter();
                }
                if (value instanceof String) {
                    return new SecretParameter((String) value);
                }
                throw new ClassCastException("参数值与类型不匹配，无法转换");
            }

            @Override
            public Parameter<?> defaultParameter() {
                return new SecretParameter("");
            }
        },
        NUMBER {
            @Override
            public Parameter<?> newParameter(Object value) {
                if (value == null) {
                    return defaultParameter();
                }
                if (value instanceof Number) {
                    return new NumberParameter(new BigDecimal(value.toString()));
                }
                throw new ClassCastException("参数值与类型不匹配，无法转换");
            }

            @Override
            public Parameter<?> defaultParameter() {
                return new NumberParameter(BigDecimal.ZERO);
            }
        };

        public static Optional<Type> getEnumInstance(String value) {
            if (value == null || value.length() < 1) {
                return Optional.empty();
            }
            for (Type t : values()) {
                if (t.name().equalsIgnoreCase(value)) {
                    return Optional.of(t);
                }
            }
            return Optional.empty();
        }

        public abstract Parameter<?> newParameter(Object value);

        public abstract Parameter<?> defaultParameter();

        public static Type getTypeByName(String typeName) {
            return Arrays.stream(Type.values())
                    .filter(t -> t.name().equals(typeName))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("定义的参数类型未找到"));
        }
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
