package dev.jianmu.parameter.aggregate;

/**
 * @class: SecretParameter
 * @description: 密钥参数
 * @author: Ethan Liu
 * @create: 2021-04-20 22:54
 **/
public class SecretParameter extends Parameter<String> {
    public SecretParameter(String value) {
        super(value);
        this.type = Type.SECRET;
    }
}
