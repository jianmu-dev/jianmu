// Generated from JianMuEl.g4 by ANTLR 4.9.1

package dev.jianmu.el.antlr4;

import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link JianMuElParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface JianMuElVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link JianMuElParser#equation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEquation(JianMuElParser.EquationContext ctx);
	/**
	 * Visit a parse tree produced by {@link JianMuElParser#literal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLiteral(JianMuElParser.LiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link JianMuElParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpression(JianMuElParser.ExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link JianMuElParser#primary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimary(JianMuElParser.PrimaryContext ctx);
}