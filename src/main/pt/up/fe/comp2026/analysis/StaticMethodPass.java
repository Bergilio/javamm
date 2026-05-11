package pt.up.fe.comp2026.analysis;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2026.jmm.ast.JmmKind;

public class StaticMethodPass extends AnalysisVisitor {
    private boolean inStaticMehod = false;

    @Override
    public void buildVisitor() {
        addVisit(JmmKind.METHOD_DECL, this::visitMethodDecl);
        addVisit(JmmKind.THIS, this::visitThis);
        addVisit(JmmKind.VAR_REF_EXPR, this::visitVarRefExpr);
    }

    private Void visitMethodDecl(JmmNode method, SymbolTable table) {
        inStaticMehod = method.getObject("isStatic", Boolean.class);
        return null;
    }

    private Void visitThis(JmmNode node, SymbolTable table) {
        if (inStaticMehod) {
            addReport(newError(node, "'this' cannot be used inside static method"));
        }
        return null;
    }

    private Void visitVarRefExpr(JmmNode node, SymbolTable table) {
        if (inStaticMehod) {
            var name = node.get("name");

            if (table.getField(name).isPresent()) {
                addReport(newError(node, "Cannot access instance field '" + name + "' from a static method"));
            }
        }
        return null;
    }
}
