package pt.up.fe.comp2026.analysis;

import pt.up.fe.comp.jmm.analysis.table.MethodSymbol;
import pt.up.fe.comp.jmm.analysis.table.Signature;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.reflection.Importer;
import pt.up.fe.comp.jmm.analysis.table.type.JmmType;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2026.ast.TypeUtils;

import java.util.ArrayList;
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
        return findMethod(table, sig, methodName, false, tU, call);
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
        return findMethod(fTable, sig, methodName, isStatic, tU, functionCall);
    }

    //Copied from TYpeUtils... I don't like it, but I don't want to instantiate it either as it will fail with imported tables...
    //I can't guarantee your sanity bellow this point... I hate Java and its inheritance, man...
    private Void findMethod(SymbolTable table, Signature sig, String methodName, boolean isStaticCall, TypeUtils tU, JmmNode call) {
        SymbolTable curTable = table;

        while(curTable != null) {
            var possibilities = curTable.getMethods(methodName);
            List<MethodSymbol> candidates = new ArrayList<>();

            for(var p : possibilities)
                if (isCompatible(p, sig, isStaticCall, tU))
                    candidates.add(p);

            //Get the best match. Long story short, this is my attempt at handling the filthy inheritance and its ambiguity
            //Like imagine, if we have a(Fruit f, Apple a), a(Fruit f, Fruit f) and then a(new Apple(), new Apple()), which should be called? Ah, Apple extends Fruit
            //And if we have a(Fruit f, Apple a), a(Apple a, Fruit f). Congratulations, different signatures but an ambiguity problem, I think.
            //Basically, we have to check which is more specific, if any
            //Do we even have to handle this kind of inheritance problem???
            if(!candidates.isEmpty())
                return mostSpecific(candidates, tU, call);

            String fqn = curTable.getSuperFullyQualifiedName();
            if(fqn == null)
                break;

            var imported = Importer.fromThisClassPath().getSymbolTableOf(fqn);
            if(imported.isEmpty())
                imported = Importer.fromThisClassPath().tryImplicitImport(fqn);

            curTable = imported.orElse(null);
        }

        addReport(newError(call, "Can't find method '" + methodName + "' with signature " + sig));
        return null;
    }

    private boolean isCompatible(MethodSymbol method, Signature sig, boolean isStaticCall, TypeUtils tU) {
        var candidateParams = method.signature().parameters();
        var reqParams = sig.parameters();

        if(candidateParams.size() != reqParams.size())
            return false;
        if(isStaticCall && !method.isStatic())
            return false;

        for(int i = 0; i < candidateParams.size(); i++)
            if(!tU.isAssignable(candidateParams.get(i), reqParams.get(i)))
                return false;

        return true;
    }

    private Void mostSpecific(List<MethodSymbol> candidates, TypeUtils tU, JmmNode call) {
        var best = candidates.getFirst();
        var ambiguous = false;

        for(int i = 1; i < candidates.size(); i++) {
            var cur = candidates.get(i);

            var curIsBetter = isMoreSpecific(cur.signature(), best.signature(), tU); //Check if cur is more specific than best
            var bestIsBetter = isMoreSpecific(best.signature(), cur.signature(), tU); //Check if best is more specific than cur

            if(curIsBetter && !bestIsBetter) {  //Basically, if we found a better one
                best = cur;
                ambiguous = false;
            }
            else if(!curIsBetter && !bestIsBetter)  //The case I mentioned in findMethod (none is more specific than the other):
                // "And if we have a(Fruit f, Apple a), a(Apple a, Fruit f). Congratulations, different signatures but an ambiguity problem, I think."
                ambiguous = true;   //It sets us up for failure (unfortunately) unless we find a new best case
        }

        if(ambiguous)
            addReport(newError(call, "Ambiguous call to method" + call.get("name") + ": multiple matches found."));

        return null;
    }

    private boolean isMoreSpecific(Signature sig1, Signature sig2, TypeUtils tU) {
        var params1 = sig1.parameters();
        var params2 = sig2.parameters();

        for (int i = 0; i < params1.size(); i++)
            // If a param of dig1 is not assignable to its sig2 counterpart, then sig1 is, probably(?), not more specific than sig2
            if (!tU.isAssignable(params2.get(i), params1.get(i))) //Note this might work (or not) because sig1 != sig2
                return false;

        return true;
    }
}