package pt.up.fe.comp2026.analysis;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.reflection.Importer;
import pt.up.fe.comp.jmm.ast.JmmNode;

import static pt.up.fe.comp2026.jmm.ast.JmmKind.IMPORT_DECL;

public class ImportsPass extends AnalysisVisitor{
    @Override
    protected void buildVisitor() {
        addVisit(IMPORT_DECL, this::visitImportDecl);
    }

    private Void visitImportDecl(JmmNode importDecl, SymbolTable symbolTable) {
        var fqn = String.join(".", importDecl.getObjectAsList("path", String.class));
        if(Importer.fromThisClassPath().inClassPath(fqn))
            return null;
        addReport(newError(importDecl, "Import " + fqn + " not found"));

        return null;
    }
}
