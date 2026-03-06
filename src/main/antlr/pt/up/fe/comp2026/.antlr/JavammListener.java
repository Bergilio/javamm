// Generated from /home/hugora/Documentos/Estudo/Comp/comp2026-1d/src/main/antlr/pt/up/fe/comp2026/Javamm.g4 by ANTLR 4.13.1

    package pt.up.fe.comp2026;

import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link JavammParser}.
 */
public interface JavammListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link JavammParser#program}.
	 * @param ctx the parse tree
	 */
	void enterProgram(JavammParser.ProgramContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavammParser#program}.
	 * @param ctx the parse tree
	 */
	void exitProgram(JavammParser.ProgramContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavammParser#importDecl}.
	 * @param ctx the parse tree
	 */
	void enterImportDecl(JavammParser.ImportDeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavammParser#importDecl}.
	 * @param ctx the parse tree
	 */
	void exitImportDecl(JavammParser.ImportDeclContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavammParser#packageDecl}.
	 * @param ctx the parse tree
	 */
	void enterPackageDecl(JavammParser.PackageDeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavammParser#packageDecl}.
	 * @param ctx the parse tree
	 */
	void exitPackageDecl(JavammParser.PackageDeclContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavammParser#classDecl}.
	 * @param ctx the parse tree
	 */
	void enterClassDecl(JavammParser.ClassDeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavammParser#classDecl}.
	 * @param ctx the parse tree
	 */
	void exitClassDecl(JavammParser.ClassDeclContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavammParser#varDecl}.
	 * @param ctx the parse tree
	 */
	void enterVarDecl(JavammParser.VarDeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavammParser#varDecl}.
	 * @param ctx the parse tree
	 */
	void exitVarDecl(JavammParser.VarDeclContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavammParser#param}.
	 * @param ctx the parse tree
	 */
	void enterParam(JavammParser.ParamContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavammParser#param}.
	 * @param ctx the parse tree
	 */
	void exitParam(JavammParser.ParamContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavammParser#type}.
	 * @param ctx the parse tree
	 */
	void enterType(JavammParser.TypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavammParser#type}.
	 * @param ctx the parse tree
	 */
	void exitType(JavammParser.TypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavammParser#methodDecl}.
	 * @param ctx the parse tree
	 */
	void enterMethodDecl(JavammParser.MethodDeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavammParser#methodDecl}.
	 * @param ctx the parse tree
	 */
	void exitMethodDecl(JavammParser.MethodDeclContext ctx);
	/**
	 * Enter a parse tree produced by the {@code AssignStmt}
	 * labeled alternative in {@link JavammParser#stmt}.
	 * @param ctx the parse tree
	 */
	void enterAssignStmt(JavammParser.AssignStmtContext ctx);
	/**
	 * Exit a parse tree produced by the {@code AssignStmt}
	 * labeled alternative in {@link JavammParser#stmt}.
	 * @param ctx the parse tree
	 */
	void exitAssignStmt(JavammParser.AssignStmtContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ReturnStmt}
	 * labeled alternative in {@link JavammParser#stmt}.
	 * @param ctx the parse tree
	 */
	void enterReturnStmt(JavammParser.ReturnStmtContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ReturnStmt}
	 * labeled alternative in {@link JavammParser#stmt}.
	 * @param ctx the parse tree
	 */
	void exitReturnStmt(JavammParser.ReturnStmtContext ctx);
	/**
	 * Enter a parse tree produced by the {@code VarRefExpr}
	 * labeled alternative in {@link JavammParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterVarRefExpr(JavammParser.VarRefExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code VarRefExpr}
	 * labeled alternative in {@link JavammParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitVarRefExpr(JavammParser.VarRefExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code BinaryExpr}
	 * labeled alternative in {@link JavammParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterBinaryExpr(JavammParser.BinaryExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code BinaryExpr}
	 * labeled alternative in {@link JavammParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitBinaryExpr(JavammParser.BinaryExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code IntegerLiteral}
	 * labeled alternative in {@link JavammParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterIntegerLiteral(JavammParser.IntegerLiteralContext ctx);
	/**
	 * Exit a parse tree produced by the {@code IntegerLiteral}
	 * labeled alternative in {@link JavammParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitIntegerLiteral(JavammParser.IntegerLiteralContext ctx);
}