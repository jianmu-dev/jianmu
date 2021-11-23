package dev.jianmu.api.vo;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageInfo;

/**
 * @class PageUtils
 * @description Page工具类
 * @author Ethan Liu
 * @create 2021-04-22 11:10
*/
public class PageUtils {
    public static <P, V> PageInfo<V> pageInfo2PageInfoVo(PageInfo<P> pageInfoPo) {
        // 创建Page对象，实际上是一个ArrayList类型的集合
        Page<V> page = new Page<>(pageInfoPo.getPageNum(), pageInfoPo.getPageSize());
        page.setTotal(pageInfoPo.getTotal());
        return new PageInfo<>(page);
    }
}
