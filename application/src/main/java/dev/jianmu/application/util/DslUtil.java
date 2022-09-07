package dev.jianmu.application.util;

import lombok.Getter;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Daihw
 * @class DslUtil
 * @description DslUtil
 * @create 2022/9/7 11:35 上午
 */
public class DslUtil {
    private static final Yaml yaml;

    static {
        LoaderOptions loaderOptions = new LoaderOptions();
        loaderOptions.setAllowDuplicateKeys(false);
        yaml = new Yaml(loaderOptions);
    }

    public static Diff diff(String dslText1, String dslText2) {
        var o1 = yaml.load(dslText1);
        var o2 = yaml.load(dslText2);

        var o1RawData = getRawData(o1);
        var o2RawData = getRawData(o2);

        var diff = new Diff();
        diff.diff = compare(o1, o2);
        diff.o1HasRawData = o1RawData != null;
        diff.o2HasRawData = o2RawData != null;
        diff.oldRawData = o1RawData;
        return diff;
    }

    private static String getRawData(Object o) {
        if (o == null) {
            return null;
        }
        if (!(o instanceof Map)) {
            return null;
        }
        return (String) ((Map<?, ?>) o).get("raw-data");
    }

    private static boolean compare(Object o1, Object o2) {
        if (o1 == null || o2 == null) {
            return Objects.equals(o1, o2);
        }
        if (o1.getClass() != o2.getClass()) {
            // 类型不同时返回false
            return false;
        }
        if (o1 instanceof List) {
            // 比较集合是否相等
            if (((List<?>) o1).size() != ((List<?>) o2).size()) {
                return false;
            }
            for (int i = 0; i < ((List<?>) o1).size(); i++) {
                if (!compare(((List<?>) o1).get(i), ((List<?>) o2).get(i))) {
                    return false;
                }
            }
        }
        if (o1 instanceof Map) {
            var map1 = ((Map<String, ?>) o1);
            var map2 = ((Map<String, ?>) o2);
            // 不比较raw-data
            map1.remove("raw-data");
            map2.remove("raw-data");
            if (map1.size() != map2.size()) {
                return false;
            }
            for (String key : map1.keySet()) {
                if (!compare(map1.get(key), map2.get(key))) {
                    return false;
                }
            }
        }
        return Objects.equals(o1, o2);
    }

    @Getter
    public static class Diff {
        private boolean diff;
        private boolean o1HasRawData;
        private boolean o2HasRawData;
        private String oldRawData;
    }
}
