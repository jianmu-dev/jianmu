package dev.jianmu.el;

import dev.jianmu.el.antlr4.JianMuElLexer;
import dev.jianmu.el.antlr4.JianMuElParser;
import dev.jianmu.el.antlr4.JianMuElVisitor;
import dev.jianmu.workflow.el.EvaluationContext;
import dev.jianmu.workflow.el.Expression;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.Optional;

/**
 * @class: El
 * @description: 表达式引擎入口类
 * @author: Ethan Liu
 * @create: 2021-01-31 17:39
 **/
public class El implements Expression {
    private EvaluationContext context;
    private ParseTree tree;
    private String expr;
    private boolean valid;

    public El(String expr) {
        // 对每一个输入的字符串，构造一个 CharStream 流 input
        CharStream input = CharStreams.fromString(expr);
        // 用 input 构造词法分析器 lexer，词法分析的作用是将字符聚集成单词或者符号
        JianMuElLexer lexer = new JianMuElLexer(input);
        // 用词法分析器 lexer 构造一个记号流 tokens
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        // 再使用 tokens 构造语法分析器 parser,至此已经完成词法分析和语法分析的准备工作
        JianMuElParser parser = new JianMuElParser(tokens);
        // 最终调用语法分析器的规则 r（这个是我们在Interpreter.g4里面定义的那个规则），完成对表达式的验证
        this.tree = parser.equation();
        this.expr = expr;
        this.valid = true;
    }

    public Object eval(EvaluationContext context) {
        if (null != this.tree) {
            this.context = context;
            return this.calculate();
        }
        throw new RuntimeException("不存在预编译的表达式");
    }

    private El(EvaluationContext context, String expr) {
        this.context = context;
        // 对每一个输入的字符串，构造一个 CharStream 流 input
        CharStream input = CharStreams.fromString(expr);
        // 用 input 构造词法分析器 lexer，词法分析的作用是将字符聚集成单词或者符号
        JianMuElLexer lexer = new JianMuElLexer(input);
        // 用词法分析器 lexer 构造一个记号流 tokens
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        // 再使用 tokens 构造语法分析器 parser,至此已经完成词法分析和语法分析的准备工作
        JianMuElParser parser = new JianMuElParser(tokens);
        // 最终调用语法分析器的规则 r（这个是我们在Interpreter.g4里面定义的那个规则），完成对表达式的验证
        this.tree = parser.equation();
        this.expr = expr;
        this.valid = true;
    }

    private Object calculate() {
        JianMuElVisitor jianMuElVisitor = new ElVisitorImpl(this.context);
        // 开始遍历语法分析树, 并返回结果
        return jianMuElVisitor.visit(tree);
    }

    public static Object eval(String expr) {
        return new El(expr).calculate();
    }

    public static Object eval(EvaluationContext context, String expr) {
        return new El(context, expr).calculate();
    }

    @Override
    public String getExpression() {
        return this.expr;
    }

    @Override
    public Optional<String> getVariableName() {
        return Optional.empty();
    }

    @Override
    public boolean isStatic() {
        return false;
    }

    @Override
    public boolean isValid() {
        return this.valid;
    }

    @Override
    public String getFailureMessage() {
        return null;
    }
}
