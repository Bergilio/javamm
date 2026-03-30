package pt.up.fe.comp2026.analysis;

import pt.up.fe.comp.jmm.analysis.table.MethodSymbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.reflection.Importer;
import pt.up.fe.comp.jmm.analysis.table.type.JmmType;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2026.ast.TypeUtils;
import pt.up.fe.comp2026.jmm.ast.JmmKind;

import static pt.up.fe.comp2026.jmm.ast.JmmKind.EXPR;

public class AssignmentsPass extends AnalysisVisitor {
    private MethodSymbol currentMethod;
    @Override
    public void buildVisitor() {
        addVisit(JmmKind.METHOD_DECL, this::visitMethodDecl);
        addVisit(JmmKind.ASSIGN_STMT, this::visitAssignStmt);
        addVisit(JmmKind.VAR_DECL, this::visitVarDecl);
    }

    private Void visitMethodDecl(JmmNode method, SymbolTable table) {
        var signature = TypeUtils.with(table).getMethodDeclSignature(method);
        currentMethod = table.getMethod(signature).orElseThrow();
        return null;
    }

    // None of this passes check the NEW stmts, methods, classes, etc. Only does the basic of base types (to expand)
    private Void visitAssignStmt(JmmNode assignStmt, SymbolTable table) {
        var varType = getVarType(assignStmt.get("var"), table);
        var exprType = TypeUtils.with(table).getExprType(assignStmt.getChildren(EXPR).getFirst());
        if(!TypeUtils.with(table).isAssignable(varType, exprType))
            addReport(newError(assignStmt, "Can't assign " + varType + " to " + exprType));
        return null;
    }

    private Void visitVarDecl(JmmNode varDecl, SymbolTable table) {
        if(varDecl.getChildren(EXPR).isEmpty())
            return null;
        var tU = TypeUtils.with(table);
        var assigneeType = tU.getExprType(varDecl.getChildren(EXPR).getFirst());
        if(!TypeUtils.with(table).isAssignable(tU.convertType(varDecl.getChild(0)), assigneeType))
            addReport(newError(varDecl, "Can't assign " + tU.convertType(varDecl.getChild(0)) + " to " + assigneeType));
        return null;
    }

    // Considering using the ones in TypeUtils
    private JmmType getVarType(String varName, SymbolTable table) {
        if(currentMethod.getLocalVariable(varName).isPresent())
            return currentMethod.getLocalVariable(varName).get().type();

        if(currentMethod.getParameter(varName).isPresent())
            return currentMethod.getParameter(varName).get().type();

        if(table.getField(varName).isPresent())
            return table.getField(varName).get().type();

        SymbolTable classTable = table;
        while(true) {
            if(classTable.getField(varName).isPresent())
                return classTable.getField(varName).get().type();
            var superClass = classTable.getSuperFullyQualifiedName();
            if(superClass == null)
                break;
            classTable = Importer.fromThisClassPath().getSymbolTableOf(superClass).orElseThrow();
        }

        throw new RuntimeException("Can't compute type for field '" + varName + "'");
        // Expand to search in imported classes
    }
}
