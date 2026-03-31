package pt.up.fe.comp2026.analysis;

import pt.up.fe.comp.jmm.analysis.table.MethodSymbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.reflection.Importer;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2026.ast.TypeUtils;
import pt.up.fe.comp2026.jmm.ast.JmmKind;

public class UndeclaredVarPass extends AnalysisVisitor {
    private MethodSymbol currentMethod;
    @Override
    public void buildVisitor() {
        addVisit(JmmKind.METHOD_DECL, this::visitMethodDecl);
        addVisit(JmmKind.VAR_REF_EXPR, this::visitVarRefExpr);
    }

    private Void visitMethodDecl(JmmNode method, SymbolTable table) {
        var signature = TypeUtils.with(table).getMethodDeclSignature(method);
        currentMethod = table.getMethod(signature).orElseThrow();
        return null;
    }

    private Void visitVarRefExpr(JmmNode varRefExpr, SymbolTable table) {
        var varRefName = varRefExpr.get("name");
        if(table.getClassName().equals(varRefName) || table.getImportedFullyQualifiedName(varRefName).isPresent() || Importer.fromThisClassPath().isImplicitImport(varRefName))
            return null;
        if(currentMethod.getParameter(varRefName).isPresent())
            return null;
        if(currentMethod.getLocalVariable(varRefName).isPresent())
            return null;
        var classTable = table;
        while(classTable != null) {
            if(classTable.getField(varRefName).isPresent())
                return null;
            var superClass = classTable.getSuperFullyQualifiedName();
            if(superClass == null)
                break;
            classTable = Importer.fromThisClassPath().getSymbolTableOf(superClass).orElseThrow();
        }
        // Expand to search in imported classes
        addReport(newError(varRefExpr, "Variable not declared"));
        return null;
    }
}
