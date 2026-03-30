package pt.up.fe.comp2026.analysis;

import pt.up.fe.comp.jmm.analysis.table.Signature;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.reflection.Importer;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2026.ast.TypeUtils;

import java.util.Optional;

import static pt.up.fe.comp2026.jmm.ast.JmmKind.*;

public class LocalCallsSemanticPass extends AnalysisVisitor {

    @Override
    protected void buildVisitor() {
        addVisit(IMPLICIT_THIS_FUNCTION_CALL, this::visitImplicitThisCall);
        addVisit(FUNCTION_CALL, this::visitFunctionCall);
        //addVisit(FIELD_ACCESS, this::visitFieldAccess);
    }
/*  TODO: PROBABLY NEEDED
    private Void visitFieldAccess(JmmNode field, SymbolTable table) {
        var fieldName = field.
    }
*/
    private Void visitImplicitThisCall(JmmNode call, SymbolTable table) {
        var methodName = call.get("func");
        var params = call.getChildren(EXPR).stream().map(p -> TypeUtils.with(table).getExprType(p)).toList();
        var sig = new Signature(methodName, params);
        return findMethod(table, call, sig, methodName);
    }

    private Void visitFunctionCall(JmmNode functionCall, SymbolTable table) {
        var methodName = functionCall.get("func");
        var target = functionCall.getChild(0);
        var params = functionCall.getChildren(EXPR).subList(1, functionCall.getChildren(EXPR).toArray().length).stream().map(p -> TypeUtils.with(table).getExprType(p)).toList();
        var sig = new Signature(methodName, params);

        var called = TypeUtils.with(table).getExprType(target);

        Optional<SymbolTable> curTable = Optional.empty();
        if(called.asClass().fullyQualifiedName().equals(table.getFullyQualifiedName()))
            curTable = Optional.of(table);
        else
            if(Importer.fromThisClassPath().isImplicitImport(called.asClass().fullyQualifiedName()))
                curTable = Importer.fromThisClassPath().tryImplicitImport(called.asClass().fullyQualifiedName());
            else if(Importer.fromThisClassPath().inClassPath(called.asClass().fullyQualifiedName()))
                curTable = Importer.fromThisClassPath().getSymbolTableOf(called.asClass().fullyQualifiedName());

        if(curTable.isEmpty()){
            addReport(newError(functionCall, "Can't find any method corresponding to " + methodName));
            return null;
        }

        var fTable = curTable.get();
        return findMethod(fTable, functionCall, sig, methodName);
    }

    private Void findMethod(SymbolTable table, JmmNode call, Signature sig, String methodName) {
        if (table.getMethod(sig).isEmpty() && table.getSuper() == null)
            addReport(newError(call, "Method '" + methodName + "' does not exist"));
        else if(table.getMethod(sig).isEmpty()) {
            var fqn = table.getSuperFullyQualifiedName();
            while(fqn != null) {
                var newTable = Importer.fromThisClassPath().getSymbolTableOf(fqn);
                if(newTable.isEmpty())
                    newTable = Importer.fromThisClassPath().tryImplicitImport(fqn);
                if(newTable.isEmpty())
                    break;

                else if(newTable.get().getMethod(sig).isPresent())
                    return null;
                fqn = newTable.get().getSuperFullyQualifiedName();
            }
            addReport(newError(call, "Method '" + methodName + "' does not exist"));
        }
        return null;
    }
}