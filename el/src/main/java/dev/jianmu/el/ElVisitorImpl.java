package dev.jianmu.el;

import dev.jianmu.el.antlr4.JianMuElBaseVisitor;
import dev.jianmu.el.antlr4.JianMuElParser;
import dev.jianmu.workflow.el.EvaluationContext;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * @class: ElVisitorImpl
 * @description: 语法树遍历实现
 * @author: Ethan Liu
 * @create: 2021-02-20 09:55
 **/
public class ElVisitorImpl extends JianMuElBaseVisitor {
    private final EvaluationContext context;

    public ElVisitorImpl(EvaluationContext context) {
        this.context = context;
    }

    @Override
    public Object visitEquation(JianMuElParser.EquationContext ctx) {
        return visit(ctx.expression());
    }

    @Override
    public Object visitLiteral(JianMuElParser.LiteralContext ctx) {
        if (null != ctx.BOOL_LITERAL()) {
            return Boolean.valueOf(ctx.BOOL_LITERAL().getText());
        }
        if (null != ctx.FLOAT_LITERAL()) {
            return new BigDecimal(ctx.FLOAT_LITERAL().getText());
        }
        // 去掉首尾双引号
        if (null != ctx.STRING_LITERAL()) {
            String s = ctx.STRING_LITERAL().getText();
            s = s.substring(1);
            s = s.substring(0, s.length() - 1);
            return s;
        }
        if (null != ctx.INT_LITERAL()) {
            return new BigDecimal(ctx.INT_LITERAL().getText());
        }
        if (null != ctx.NULL_LITERAL()) {
            return null;
        }
        throw new RuntimeException("字面量解析错误: " + ctx.getText());
    }

    @Override
    public Object visitExpression(JianMuElParser.ExpressionContext ctx) {
        // 最小词法单元解析
        if (ctx.expression().size() == 0) {
            return visit(ctx.primary());
        }
        //
        if (ctx.prefix != null) {
            Object value = visit(ctx.expression(0));
            return this.notOperation(value, ctx.prefix.getType(), ctx.prefix.getText());
        }
        // 表达式关系等式运算
        Object left = visit(ctx.expression(0));
        Object right = visit(ctx.expression(1));
        // null 运算
        if (null == left || null == right) {
            return this.nullOperation(left, right, ctx.bop.getType(), ctx.bop.getText());
        }
        // 数字运算
        if (left instanceof BigDecimal && right instanceof BigDecimal) {
            return this.arithmetic((BigDecimal) left, (BigDecimal) right, ctx.bop.getType(), ctx.bop.getText());
        }
        // 布尔运算
        if (left instanceof Boolean && right instanceof Boolean) {
            return this.logic((Boolean) right, (Boolean) left, ctx.bop.getType(), ctx.bop.getText());
        }
        // 字符串运算
        if (left instanceof String && right instanceof String) {
            return this.string((String) right, (String) left, ctx.bop.getType(), ctx.bop.getText());
        }
        return this.typeCasting(right, left, ctx.bop.getType(), ctx.getText());
    }

    @Override
    public Object visitPrimary(JianMuElParser.PrimaryContext ctx) {
        if (null != ctx.literal()) {
            return visit(ctx.literal());
        }
        if (null != ctx.expression()) {
            return visit(ctx.expression());
        }
        if (null != ctx.VARNAME()) {
            // 去掉头尾的$和{}
            var variableName = ctx.VARNAME().getText();
            variableName = variableName.substring(2);
            variableName = variableName.substring(0, variableName.length() - 1);
            return this.context.getVariable(variableName);
        }
        if (null != ctx.TEMPLATE()) {
            return this.template(ctx.TEMPLATE().getText());
        }
        throw new RuntimeException("Primary解析错误: " + ctx.getText());
    }

    private Boolean notOperation(Object value, int flag, String op) {
        if (!(value instanceof Boolean)) {
            throw new RuntimeException("非Boolean值不支持该操作符" + op);
        }
        if (flag == JianMuElParser.NOT) {
            var o = (Boolean) value;
            return !o;
        }
        throw new RuntimeException("Boolean值不支持该操作符" + op);
    }

    private Boolean nullOperation(Object left, Object right, int flag, String op) {
        if (flag == JianMuElParser.EQ) {
            return left == right;
        }
        if (flag == JianMuElParser.NE) {
            return left != right;
        }
        throw new RuntimeException("null不支持使用该操作符" + op);
    }

    private Object fieldValue(Object left, String filedName) {
        Object value = ReflectUntil.getFieldValue(left, filedName);
        if (value instanceof Integer) {
            return BigDecimal.valueOf((Integer) value);
        }
        if (value instanceof Long) {
            return BigDecimal.valueOf((Long) value);
        }
        if (value instanceof Float) {
            String v = Float.toString((Float) value);
            return new BigDecimal(v);
        }
        if (value instanceof Double) {
            String v = Double.toString((Double) value);
            return new BigDecimal(v);
        }
        if (value instanceof Byte) {
            throw new RuntimeException("不支持获取Byte类型的属性");
        }
        if (value instanceof Short) {
            throw new RuntimeException("不支持获取Short类型的属性");
        }
        if (value instanceof Character) {
            throw new RuntimeException("不支持获取Character类型的属性");
        }
        return ReflectUntil.getFieldValue(left, filedName);
    }

    private Object methodCall(Object left, Object right, String methodName) {
        List<Object> args = (List) right;
        return ReflectUntil.invokeMethod(left, methodName, args);
    }

    private Object string(String left, String right, int flag, String op) {
        if (flag == JianMuElParser.EQ) {
            return left.equals(right);
        }
        if (flag == JianMuElParser.NE) {
            return !left.equals(right);
        }
        if (flag == JianMuElParser.PLUS) {
            return right + left;
        }
        throw new RuntimeException("字符串不支持使用该操作符: " + op);
    }

    private Boolean logic(Boolean left, Boolean right, int flag, String op) {
        if (flag == JianMuElParser.EQ) {
            return left == right;
        }
        if (flag == JianMuElParser.NE) {
            return left != right;
        }
        if (flag == JianMuElParser.AND) {
            return left && right;
        }
        if (flag == JianMuElParser.OR) {
            return left || right;
        }
        throw new RuntimeException("布尔值不支持使用该操作符" + op);
    }

    private Object arithmetic(BigDecimal left, BigDecimal right, int flag, String op) {
        if (flag == JianMuElParser.TIMES) {
            return left.multiply(right);
        }
        if (flag == JianMuElParser.DIV) {
            return left.divide(right, 2, RoundingMode.HALF_UP);
        }
        if (flag == JianMuElParser.MODULO) {
            return left.divideAndRemainder(right)[1];
        }
        if (flag == JianMuElParser.PLUS) {
            return left.add(right);
        }
        if (flag == JianMuElParser.MINUS) {
            return left.subtract(right);
        }

        if (flag == JianMuElParser.EQ) {
            return left.compareTo(right) == 0;
        }
        if (flag == JianMuElParser.NE) {
            return left.compareTo(right) != 0;
        }
        if (flag == JianMuElParser.GT) {
            return left.compareTo(right) > 0;
        }
        if (flag == JianMuElParser.GE) {
            return left.compareTo(right) > -1;
        }
        if (flag == JianMuElParser.LT) {
            return left.compareTo(right) < 0;
        }
        if (flag == JianMuElParser.LE) {
            return left.compareTo(right) < 1;
        }
        throw new RuntimeException("数字不支持使用该操作符" + op);
    }

    private Object typeCasting(Object left, Object right, int flag, String exp) {
        if (!(flag == JianMuElParser.PLUS)) {
            throw new RuntimeException("不支持此类运算: " + exp);
        }
        return right.toString() + left.toString();
    }

    private String template(String template) {
        template = template.substring(1);
        template = template.substring(0, template.length() - 1);
        var resolver = PlaceholderResolver.getDefaultResolver();
        return resolver.resolveByContext(template, this.context);
    }
}
