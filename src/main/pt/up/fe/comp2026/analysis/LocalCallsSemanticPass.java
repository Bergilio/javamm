package pt.up.fe.comp2026.analysis;

import pt.up.fe.comp.jmm.analysis.table.MethodSymbol;
import pt.up.fe.comp.jmm.analysis.table.Signature;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.reflection.Importer;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2026.ast.TypeUtils;

import java.util.List;
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
        var tU = TypeUtils.with(table);
        var methodName = call.get("func");
        var params = call.getChildren(EXPR).stream().map(p -> TypeUtils.with(table).getExprType(p)).toList();
        var sig = new Signature(methodName, params);
        return findMethod(table, call, sig, methodName, false, tU);
    }

    private Void visitFunctionCall(JmmNode functionCall, SymbolTable table) {
        var tU = TypeUtils.with(table);
        var methodName = functionCall.get("func");
        var target = functionCall.getChild(0);
        var params = functionCall.getChildren(EXPR).subList(1, functionCall.getChildren(EXPR).toArray().length).stream().map(p -> TypeUtils.with(table).getExprType(p)).toList();
        var sig = new Signature(methodName, params);

        var called = TypeUtils.with(table).getExprType(target);
        var isStatic = called.asClass().staticRef();

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
        return findMethod(fTable, functionCall, sig, methodName, isStatic, tU);
    }

    private Void findMethod(SymbolTable table, JmmNode call, Signature sig, String methodName, boolean isStaticCall, TypeUtils tU) {
        if(matchInheritance(table, sig, methodName, isStaticCall, tU))
            return null;
        addReport(newError(call, "Can't find method with " + sig.toString()));
        return null;
    }

    private boolean matchInheritance(SymbolTable table, Signature sig, String methodName, boolean isStaticCall, TypeUtils tU) {
        if(match(table, methodName, sig, isStaticCall, tU))
            return true;
        if(table.getSuper() != null) {
            var fqn = table.getSuperFullyQualifiedName();
            while(fqn != null) {
                var newTable = Importer.fromThisClassPath().getSymbolTableOf(fqn);
                if(newTable.isEmpty())
                    newTable = Importer.fromThisClassPath().tryImplicitImport(fqn);
                if(newTable.isEmpty())
                    break;
                if(match(newTable.get(), methodName, sig, isStaticCall, tU))
                    return true;
                fqn = newTable.get().getSuperFullyQualifiedName();
            }
        }
        return false;
    }

    private boolean match(SymbolTable table, String methodName, Signature sig, boolean isStaticCall, TypeUtils tU) {
        var possibilities = table.getMethods(methodName);
        var callParams = sig.parameters();

        for(var p : possibilities) {
            var candidateParams = p.signature().parameters();
            if(candidateParams.size() != callParams.size())
                continue;

            if(isStaticCall && !p.isStatic())
                continue;

            boolean matchFound = true;
            for(int i = 0; i < candidateParams.size(); i++)
                if(!tU.isAssignable(candidateParams.get(i), callParams.get(i))) {
                    matchFound = false;
                    break;
                }

            if(matchFound)
                return true;

        }
        return false;
    }
}