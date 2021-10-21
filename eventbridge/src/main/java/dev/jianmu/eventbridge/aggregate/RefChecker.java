package dev.jianmu.eventbridge.aggregate;

import java.util.regex.Pattern;

/**
 * @class: RefChecker
 * @description: RefChecker
 * @author: Ethan Liu
 * @create: 2021-10-03 12:46
 **/
public class RefChecker {
    public static final String REF_REG = "^[a-zA-Z][a-zA-Z0-9_]{3,29}$";

    public static void check(String ref) {
        var result = Pattern.matches(REF_REG, ref);
        if (!result) {
            throw new RuntimeException("唯一标识以英文字母开头，且由下划线、数字和英文字母组成的4-30位字符串");
        }
    }
}
