package pt.up.fe.comp2026.analysis;

import pt.up.fe.comp.jmm.analysis.table.MethodSymbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.type.impls.JmmPrimitiveType;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2026.ast.TypeUtils;

import static pt.up.fe.comp2026.jmm.ast.JmmKind.*;

public class ReturnSemanticPass extends AnalysisVisitor {
    private MethodSymbol currentMethod = null;
    @Override
    protected void buildVisitor() {
        addVisit(METHOD_DECL, this::visitMethodDecl);
        addVisit(RETURN_STMT, this::visitReturnStmt);
    }

    private Void visitMethodDecl(JmmNode methodDecl, SymbolTable table) {
        var returnType = TypeUtils.with(table).getMethodType(methodDecl);
        var returns = methodDecl.getDescendants(RETURN_STMT);
        if (returns.isEmpty() && !returnType.equals(TypeUtils.voidType()))
            addReport(newError(methodDecl, "Method must have a return statement"));

        var signature = TypeUtils.with(table).getMethodDeclSignature(methodDecl);
        currentMethod = table.getMethod(signature).orElseThrow();
        return null;
    }

    private Void visitReturnStmt(JmmNode returnStmt, SymbolTable table) {
        if(currentMethod == null) {
            addReport(newError(returnStmt, "Return statement must correspond to a method declaration"));
            return null;
        }
        var expectedType = currentMethod.returnType();
        if (returnStmt.getChildren().isEmpty() && !expectedType.equals(JmmPrimitiveType.VOID)){
            addReport(newError(returnStmt, "Method must return a non-null value."));
            return null;
        }
        else if (!returnStmt.getChildren().isEmpty() && expectedType.equals(JmmPrimitiveType.VOID)) {
            addReport(newError(returnStmt, "Method can't return anything."));
            return null;
        }
        else if(expectedType.equals(JmmPrimitiveType.VOID))
            return null;
        var expr = returnStmt.getChild(0);
        var actualType = TypeUtils.with(table).getExprType(expr);
        if (!TypeUtils.with(table).isAssignable(actualType, expectedType))
            addReport(newError(returnStmt, "Return type mismatch"));

        return null;
    }
}