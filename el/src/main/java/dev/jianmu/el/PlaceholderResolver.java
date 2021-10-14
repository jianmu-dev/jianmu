package dev.jianmu.el;

import dev.jianmu.workflow.el.EvaluationContext;

import java.util.function.Function;

/**
 * @class: PlaceholderResolver
 * @description: 占位符解析器
 * @author: Ethan Liu
 * @create: 2021-09-02 08:45
 **/
public class PlaceholderResolver {
    /**
     * 默认前缀占位符
     */
    public static final String DEFAULT_PLACEHOLDER_PREFIX = "${";

    /**
     * 默认后缀占位符
     */
    public static final String DEFAULT_PLACEHOLDER_SUFFIX = "}";

    /**
     * 默认单例解析器
     */
    private static PlaceholderResolver defaultResolver = new PlaceholderResolver();

    /**
     * 占位符前缀
     */
    private String placeholderPrefix = DEFAULT_PLACEHOLDER_PREFIX;

    /**
     * 占位符后缀
     */
    private String placeholderSuffix = DEFAULT_PLACEHOLDER_SUFFIX;

    private PlaceholderResolver() {
    }

    private PlaceholderResolver(String placeholderPrefix, String placeholderSuffix) {
        this.placeholderPrefix = placeholderPrefix;
        this.placeholderSuffix = placeholderSuffix;
    }

    /**
     * 获取默认的占位符解析器，即占位符前缀为"${", 后缀为"}"
     *
     * @return
     */
    public static PlaceholderResolver getDefaultResolver() {
        return defaultResolver;
    }

    public static PlaceholderResolver getResolver(String placeholderPrefix, String placeholderSuffix) {
        return new PlaceholderResolver(placeholderPrefix, placeholderSuffix);
    }

    /**
     * 根据替换规则来替换指定模板中的占位符值
     *
     * @param content 要解析的字符串
     * @param rule    解析规则回调
     * @return
     */
    public String resolveByRule(String content, Function<String, String> rule) {
        int start = content.indexOf(this.placeholderPrefix);
        if (start == -1) {
            return content;
        }
        StringBuilder result = new StringBuilder(content);
        while (start != -1) {
            int end = result.indexOf(this.placeholderSuffix, start);
            //获取占位符属性值，如${id}, 即获取id
            String placeholder = result.substring(start + this.placeholderPrefix.length(), end);
            //替换整个占位符内容，即将${id}值替换为替换规则回调中的内容
            String replaceContent = placeholder.trim().isEmpty() ? "" : rule.apply(placeholder);
            result.replace(start, end + this.placeholderSuffix.length(), replaceContent);
            start = result.indexOf(this.placeholderPrefix, start + replaceContent.length());
        }
        return result.toString();
    }

    /**
     * 替换模板中占位符内容，占位符的内容即为EvaluationContext的变量名对应的值，key为占位符中的内容。<br/><br/>
     * 如：content = product:${id}:detail:${did}<br/>
     * valueMap = id -> 1; pid -> 2<br/>
     * 经过解析返回 product:1:detail:2<br/>
     *
     * @param content 模板内容。
     * @param context 值映射
     * @return 替换完成后的字符串。
     */
    public String resolveByContext(String content, final EvaluationContext context) {
        return resolveByRule(content, placeholderValue -> String.valueOf(context.getVariable(placeholderValue)));
    }
}
