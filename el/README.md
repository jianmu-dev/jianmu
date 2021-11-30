# 建木表达式引擎

### 操作符

建木表达式引擎使用Antlr 4工具构建，当前支持的操作符如下：

| 符号 | 名称 | 解释 |
| :-----| ----: | :----: |
| () | 括号对 | 优先计算括号对中的值 |
| * | 乘 | 乘法操作符 |
| / | 整除 | 整除操作符 |
| % | 取模 | 取模操作符 |
| + | 加 | 加法操作符 |
| - | 减 | 减法操作符 |
| > | 大于 | 大于操作符 |
| < | 小于 | 小于操作符 |
| >= | 大于等于 | 大于等于操作符 |
| <= | 小于等于 | 小于等于操作符 |
| == | 等于 | 等于操作符 |
| != | 不等于 | 不等于操作符 |
| && | 逻辑与 | 逻辑与操作符 |
| &#124;&#124; | 逻辑或 | 逻辑与操作符 |

### 数据类型

当前支持的数据类型如下:

| 名称 | 取值范围 | 解释 |
| :-----| ----: | :----: |
| 数字 | unscaledValue × 10-scale | 等于Java中的BigDecimal，由任意精度的整数非标度值 和 32 位的整数标度 (scale) 组成 |
| 字符串 | utf8 | utf8字符集范围，支持使用转义字符 |
| 布尔值 | true/false | 不解释，多余 |
| 空值 | null | 就是Java里的null，不解释 |

### 返回值

关于表达式返回值

1、数学计算返回值是BigDecimal类型，后续如需使用自行转换类型

2、关系运算返回值是Boolean类型

3、字符串相关的当然还是String

4、也可能会返回null

### 表达式示例

* 四则运算

```
(12 + 33) * (3.1 - 3.142) / 0.3 = -6.30
```

* 字符串模版

```
${a} = aaa
${b} = bbb
`${a} / ${b}` = aaa / bbb
```

* 隐式类型转换
```
"abc" + 123.24 = abc123.24
```

* 复合运算
```
${a} = aaa
${b} = 32.3
${c} = false
${d} = 3
${a} +"---" +  ${b} * ${d} + "---" + `${a} != ${b} == ${c}` = aaa---96.9---aaa != 32.3 == false
```

### 已知问题

当前表达式中的数字不支持正负号，但是计算结果有可能为负数

数字进行除法运算时，结果限定为2位小数，舍入规则为四舍五入