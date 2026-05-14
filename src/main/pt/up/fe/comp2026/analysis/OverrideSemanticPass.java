package pt.up.fe.comp2026.analysis;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.reflection.Importer;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2026.ast.TypeUtils;
import pt.up.fe.comp2026.jmm.ast.JmmKind;

public class OverrideSemanticPass extends AnalysisVisitor {

    @Override
    protected void buildVisitor() {
        addVisit(JmmKind.METHOD_DECL, this::visitMethodDecl);
    }

    private Void visitMethodDecl(JmmNode methodDecl, SymbolTable table) {
        var superFQN = table.getSuperFullyQualifiedName();
        if (superFQN == null) return null;

        var superTable = Importer.fromThisClassPath().getSymbolTableOf(superFQN).or(() -> Importer.fromThisClassPath().tryImplicitImport(superFQN)).orElse(null);
        if (superTable == null) return null;

        var signature = TypeUtils.with(table).getMethodDeclSignature(methodDecl);
        var superMethod = superTable.getMethod(signature);
        if (superMethod.isEmpty()) return null;

        var actualReturn = TypeUtils.with(table).getMethodType(methodDecl);
        var expectedReturn = superMethod.get().returnType();

        if (!TypeUtils.with(table).isAssignable(actualReturn, expectedReturn))
            addReport(newError(methodDecl, "Method '" + methodDecl.get("name") +
                    "' overrides a method from '" + superFQN +
                    "' with incompatible return type"));

        return null;
    }
}