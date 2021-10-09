// Generated from JianMuEl.g4 by ANTLR 4.9.1

package dev.jianmu.el.antlr4;

import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link JianMuElParser}.
 */
public interface JianMuElListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link JianMuElParser#equation}.
	 * @param ctx the parse tree
	 */
	void enterEquation(JianMuElParser.EquationContext ctx);
	/**
	 * Exit a parse tree produced by {@link JianMuElParser#equation}.
	 * @param ctx the parse tree
	 */
	void exitEquation(JianMuElParser.EquationContext ctx);
	/**
	 * Enter a parse tree produced by {@link JianMuElParser#literal}.
	 * @param ctx the parse tree
	 */
	void enterLiteral(JianMuElParser.LiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link JianMuElParser#literal}.
	 * @param ctx the parse tree
	 */
	void exitLiteral(JianMuElParser.LiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link JianMuElParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterExpression(JianMuElParser.ExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link JianMuElParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitExpression(JianMuElParser.ExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link JianMuElParser#primary}.
	 * @param ctx the parse tree
	 */
	void enterPrimary(JianMuElParser.PrimaryContext ctx);
	/**
	 * Exit a parse tree produced by {@link JianMuElParser#primary}.
	 * @param ctx the parse tree
	 */
	void exitPrimary(JianMuElParser.PrimaryContext ctx);
}