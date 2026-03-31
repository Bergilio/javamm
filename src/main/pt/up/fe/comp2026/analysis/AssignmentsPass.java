package pt.up.fe.comp2026.analysis;

import pt.up.fe.comp.jmm.analysis.table.MethodSymbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.reflection.Importer;
import pt.up.fe.comp.jmm.analysis.table.type.JmmType;
import pt.up.fe.comp.jmm.analysis.table.type.impls.JmmArrayType;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2026.ast.TypeUtils;
import pt.up.fe.comp2026.jmm.ast.JmmKind;

import static pt.up.fe.comp2026.jmm.ast.JmmKind.*;

public class AssignmentsPass extends AnalysisVisitor {
    private MethodSymbol currentMethod;
    @Override
    public void buildVisitor() {
        addVisit(JmmKind.METHOD_DECL, this::visitMethodDecl);
        addVisit(JmmKind.ASSIGN_STMT, this::visitAssignStmt);
        addVisit(JmmKind.ASSIGN_ARRAY_ELEMENT_STMT, this::visitArrayAssignStmt);
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
            addReport(newError(assignStmt, "Can't assign " + exprType + " to " + varType));
        return null;
    }

    private Void visitArrayAssignStmt(JmmNode arrayAssignStmt, SymbolTable table) {
        var elType = getArrayType(arrayAssignStmt.get("var"), table);
        var dim = arrayAssignStmt.getChildren(ARRAY_INDEX).size();
        if(elType.asArray().dimension() < dim) {
            addReport(newError(arrayAssignStmt, "The dimension of " + arrayAssignStmt.get("var") + "is less than " + dim));
            return null;
        }
        else if(elType.asArray().dimension() > dim)
            elType = new JmmArrayType(elType.asArray().itemType(), elType.asArray().dimension() - dim);
        else
            elType = elType.asArray().itemType();

        var exprType = TypeUtils.with(table).getExprType(arrayAssignStmt.getChildren().getLast());
        if(!TypeUtils.with(table).isAssignable(elType, exprType))
            addReport(newError(arrayAssignStmt, "Can't assign " + exprType + " to " + arrayAssignStmt));
        return null;
    }

    private Void visitVarDecl(JmmNode varDecl, SymbolTable table) {
        if(varDecl.getChildren(EXPR).isEmpty())
            return null;
        var tU = TypeUtils.with(table);
        var assigneeType = tU.getExprType(varDecl.getChildren(EXPR).getFirst());
        if(!TypeUtils.with(table).isAssignable(tU.convertType(varDecl.getChild(0)), assigneeType))
            addReport(newError(varDecl, "Can't assign " + assigneeType + " to " + tU.convertType(varDecl.getChild(0))));
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

    private JmmType getArrayType(String varName, SymbolTable table) {
        if(currentMethod.getLocalVariable(varName).isPresent())
            return currentMethod.getLocalVariable(varName).get().type().asArray();

        if(currentMethod.getParameter(varName).isPresent())
            return currentMethod.getParameter(varName).get().type().asArray();

        if(table.getField(varName).isPresent())
            return table.getField(varName).get().type().asArray();

        SymbolTable classTable = table;
        while(true) {
            if(classTable.getField(varName).isPresent())
                return classTable.getField(varName).get().type().asArray();
            var superClass = classTable.getSuperFullyQualifiedName();
            if(superClass == null)
                break;
            classTable = Importer.fromThisClassPath().getSymbolTableOf(superClass).orElseThrow();
        }

        throw new RuntimeException("Can't compute type for field '" + varName + "'");
    }
}
