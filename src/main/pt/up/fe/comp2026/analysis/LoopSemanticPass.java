package pt.up.fe.comp2026.analysis;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.type.JmmType;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2026.ast.TypeUtils;
import pt.up.fe.comp.jmm.analysis.table.reflection.Importer;

import static pt.up.fe.comp2026.jmm.ast.JmmKind.*;

public class LoopSemanticPass extends AnalysisVisitor {

    @Override
    protected void buildVisitor() {
        addVisit(WHILE_STMT, this::visitWhileStmt);
        addVisit(DO_WHILE_STMT, this::visitDoWhileStmt);
        addVisit(FOR_STMT, this::visitForStmt);
    }

    private Void visitWhileStmt(JmmNode whileStmt, SymbolTable table) {
        var condition = whileStmt.getChild(0);

        var type = TypeUtils.with(table).getExprType(condition);

        if (!type.equals(TypeUtils.boolType())) 
            addReport(newError(condition, "While condition must be boolean"));
        return null;
    }

    private Void visitDoWhileStmt(JmmNode doWhileStmt, SymbolTable table) {
        var condition = doWhileStmt.getChildren(EXPR).getFirst();
        var type = TypeUtils.with(table).getExprType(condition);
        if (!type.equals(TypeUtils.boolType()))
            addReport(newError(condition, "Do While condition must be boolean"));

        return null;
    }

    private Void visitForStmt(JmmNode forStmt, SymbolTable table) {
        var condition = forStmt.getChildren(EXPR);
        if(condition.isEmpty())
            return null;
        var condition2 = condition.getFirst();

        var type = TypeUtils.with(table).getExprType(condition2);

        if (!type.equals(TypeUtils.boolType()))
            addReport(newError(condition2, "For condition must be boolean"));

        return null;
    }
}