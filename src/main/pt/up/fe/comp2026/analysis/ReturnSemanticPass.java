package pt.up.fe.comp2026.analysis;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2026.ast.TypeUtils;

import static pt.up.fe.comp2026.jmm.ast.JmmKind.*;

public class ReturnSemanticPass extends AnalysisVisitor {

    @Override
    protected void buildVisitor() {
        addVisit(METHOD_DECL, this::visitMethodDecl);
        addVisit(RETURN_STMT, this::visitReturnStmt);
    }

    private Void visitMethodDecl(JmmNode methodDecl, SymbolTable table) {
        var returnType = TypeUtils.with(table).getMethodType(methodDecl);

        if (returnType.equals(TypeUtils.voidType())) {
            return null;
        }

        var returns = methodDecl.getDescendants(RETURN_STMT);
        if (returns.isEmpty()) {
            addReport(newError(methodDecl, "Method must have a return statement"));
        }
        return null;
    }

    private Void visitReturnStmt(JmmNode returnStmt, SymbolTable table) {
        var methodDecl = returnStmt.getAncestor(METHOD_DECL).orElseThrow();

        var expectedType = TypeUtils.with(table).getMethodType(methodDecl);

        if (returnStmt.getChildren().isEmpty()) {
            return null;
        }

        var expr = returnStmt.getChild(0);
        var actualType = TypeUtils.with(table).getExprType(expr);

        if (!actualType.equals(expectedType)) {
            addReport(newError(returnStmt, "Return type mismatch"));
        }
        return null;
    }
}