package pt.up.fe.comp2026.analysis;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2026.ast.TypeUtils;

import static pt.up.fe.comp2026.jmm.ast.JmmKind.*;

public class IfElseSemanticPass extends AnalysisVisitor {

    @Override
    protected void buildVisitor() {
        addVisit(IF_STMT, this::visitIfStmt);
    }

    private Void visitIfStmt(JmmNode ifStmt, SymbolTable table) {
        var condition = ifStmt.getChild(0);

        var type = TypeUtils.with(table).getExprType(condition);

        if (!type.equals(TypeUtils.boolType())) {
            addReport(newError(condition, "If condition must be boolean"));
        }
        return null;
    }
}