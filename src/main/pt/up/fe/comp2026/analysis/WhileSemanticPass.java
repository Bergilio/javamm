package pt.up.fe.comp2026.analysis;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2026.ast.TypeUtils;

import static pt.up.fe.comp2026.jmm.ast.JmmKind.*;

public class WhileSemanticPass extends AnalysisVisitor {

    @Override
    protected void buildVisitor() {
        addVisit(WHILE_STMT, this::visitWhileStmt);
    }

    private Void visitWhileStmt(JmmNode whileStmt, SymbolTable table) {
        var condition = whileStmt.getChild(0);

        var type = TypeUtils.with(table).getExprType(condition);

        if (!type.equals(TypeUtils.boolType())) {
            addReport(newError(condition, "While condition must be boolean"));
        }
        return null;
    }
}
