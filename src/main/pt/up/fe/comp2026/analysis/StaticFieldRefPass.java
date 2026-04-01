package pt.up.fe.comp2026.analysis;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2026.ast.TypeUtils;
import pt.up.fe.comp2026.jmm.ast.JmmKind;

public class StaticFieldRefPass extends AnalysisVisitor {
    public void buildVisitor() {
        addVisit(JmmKind.FIELD_ACCESS, this::visitFieldAccess);
    }

    private Void visitFieldAccess(JmmNode field, SymbolTable table) {
        var access = TypeUtils.with(table).getExprType(field.getChild(0));
        if(access.asClass().staticRef())
            addReport(newError(field, "Trying to access class field from non-instanced class"));
        return null;
    }

}
