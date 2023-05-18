package dev.jianmu.workflow.aggregate.parameter;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

/**
 * @author Ethan Liu
 * @program: Parameter
 * @description 参数类
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
                    var s = (String) value;
                    var l = s.getBytes(StandardCharsets.UTF_8).length;
                    if (l > 65535) {
                        throw new ClassCastException("参数长度为" + l + "已超过最大长度(65535个字节)");
                    }
                    return new StringParameter(s);
                }
                throw new ClassCastException("参数值与类型不匹配，无法转换");
            }

            @Override
            public Parameter<?> newParameter(Object value, boolean isDefault) {
                if (value == null) {
                    return defaultParameter(isDefault);
                }
                if (value instanceof String) {
                    var s = (String) value;
                    var l = s.getBytes(StandardCharsets.UTF_8).length;
                    if (l > 65535) {
                        throw new ClassCastException("参数长度为" + l + "已超过最大长度(65535个字节)");
                    }
                    return new StringParameter(s, isDefault);
                }
                throw new ClassCastException("参数值与类型不匹配，无法转换");
            }

            @Override
            public Parameter<?> defaultParameter() {
                return new StringParameter("");
            }

            @Override
            public Parameter<?> defaultParameter(boolean isDefault) {
                return new StringParameter("", isDefault);
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
            public Parameter<?> newParameter(Object value, boolean isDefault) {
                if (value == null) {
                    return defaultParameter(isDefault);
                }
                if (value instanceof Boolean) {
                    return new BoolParameter((Boolean) value, isDefault);
                }
                throw new ClassCastException("参数值与类型不匹配，无法转换");
            }

            @Override
            public Parameter<?> defaultParameter() {
                return new BoolParameter(false);
            }

            @Override
            public Parameter<?> defaultParameter(boolean isDefault) {
                return new BoolParameter(false, isDefault);
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
            public Parameter<?> newParameter(Object value, boolean isDefault) {
                if (value == null) {
                    return defaultParameter(isDefault);
                }
                if (value instanceof String) {
                    return new SecretParameter((String) value, isDefault);
                }
                throw new ClassCastException("参数值与类型不匹配，无法转换");
            }

            @Override
            public Parameter<?> defaultParameter() {
                return new SecretParameter("");
            }

            @Override
            public Parameter<?> defaultParameter(boolean isDefault) {
                return new SecretParameter("", isDefault);
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
            public Parameter<?> newParameter(Object value, boolean isDefault) {
                if (value == null) {
                    return defaultParameter(isDefault);
                }
                if (value instanceof Number) {
                    return new NumberParameter(new BigDecimal(value.toString()), isDefault);
                }
                throw new ClassCastException("参数值与类型不匹配，无法转换");
            }

            @Override
            public Parameter<?> defaultParameter() {
                return new NumberParameter(BigDecimal.ZERO);
            }

            @Override
            public Parameter<?> defaultParameter(boolean isDefault) {
                return new NumberParameter(BigDecimal.ZERO, isDefault);
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

        public abstract Parameter<?> newParameter(Object value, boolean isDefault);

        public abstract Parameter<?> defaultParameter();

        public abstract Parameter<?> defaultParameter(boolean isDefault);

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
    // 是否默认值
    protected final boolean isDefault;

    protected Parameter(T value) {
        this.value = value;
        this.isDefault = false;
    }

    protected Parameter(T value, boolean isDefault) {
        this.value = value;
        this.isDefault = isDefault;
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

    public boolean isDefault() {
        return isDefault;
    }
}
