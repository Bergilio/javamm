package pt.up.fe.comp2026.optimization;

import pt.up.fe.comp.jmm.analysis.table.MethodSymbol;
import pt.up.fe.comp.jmm.analysis.table.Signature;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.JmmNodeImpl;
import pt.up.fe.comp.jmm.ast.PostorderJmmVisitor;
import pt.up.fe.comp2026.ast.TypeUtils;
import pt.up.fe.comp2026.jmm.ast.JmmKind;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static pt.up.fe.comp2026.jmm.ast.JmmKind.*;

public class AstConstantPropagationVisitor extends PostorderJmmVisitor<Void, Void> {
    private boolean changed = false;
    private HashMap<String, JmmNode> definedVars = new HashMap<>();
    private final HashMap<Signature, JmmNode> definedMethodReturns = new HashMap<>();
    final private SymbolTable table;

    public AstConstantPropagationVisitor(SymbolTable table){
        this.table = table;
    }

    @Override
    protected void buildVisitor() {
        addVisit(JmmKind.METHOD_DECL, this::visitMethodDecl);
        addVisit(JmmKind.VAR_REF_EXPR, this::visitVarRef);
        addVisit(JmmKind.ASSIGN_STMT, this::visitAssign);
        addVisit(JmmKind.VAR_DECL, this::visitVarDecl);
        setDefaultVisit(this::defaultVisit);
    }

    private Void defaultVisit(JmmNode node, Void unused) {
        return null;
    }

    private Void visitMethodDecl(JmmNode methodDecl, Void unused) {
        definedVars = new HashMap<>();
        var returnType = TypeUtils.with(table).getMethodType(methodDecl);
        if(returnType.equals(TypeUtils.voidType()))
            return null;
        var returnValue = methodDecl.getChild(methodDecl.getNumChildren() - 1).getChild(0);
        if(returnValue.getKind().equals(JmmKind.INTEGER_LITERAL) || returnValue.getKind().equals(JmmKind.BOOLEAN_LITERAL))
            definedMethodReturns.put(TypeUtils.with(table).getMethodDeclSignature(methodDecl), returnValue);

        return null;
    }

    private Void visitVarRef(JmmNode varRef, Void unused) {
        var varName = varRef.get("name");
        if(definedVars.containsKey(varName)){
            var value = definedVars.get(varName);
            var newNode = new JmmNodeImpl(value.getKind());
            newNode.put("value", value.get("value"));
            varRef.replace(newNode);
            varRef.removeParent();
            changed = true;
        }
        return null;
    }

    private Void visitAssign(JmmNode assign, Void unused) {
        var value = assign.getChild(0);
        if(value.isInstance(JmmKind.INTEGER_LITERAL) || value.isInstance(JmmKind.BOOLEAN_LITERAL)) {
            if (definedVars.containsKey(assign.get("var"))) {
                var ancestor = assign.getAncestor(IF_STMT);
                if (ancestor.isPresent()) {
                    definedVars.remove(assign.get("var"));
                    return null;
                }
                ancestor = assign.getAncestor(WHILE_STMT);
                if(ancestor.isPresent()) {
                    definedVars.remove(assign.get("var"));
                    return null;
                }
                definedVars.replace(assign.get("var"), value);
            }

            else {
                var ancestor = assign.getAncestor(IF_STMT);
                if (ancestor.isPresent())
                    return null;
                ancestor = assign.getAncestor(WHILE_STMT);
                if(ancestor.isPresent())
                    return null;
                definedVars.put(assign.get("var"), value);
            }
        }
        else definedVars.remove(assign.get("var"));
        return null;
    }

    private Void visitVarDecl(JmmNode varDecl, Void unused) {
        if(varDecl.getChildren().size() == 1)
            return null; // No Assign
        var value = varDecl.getChild(1);
        if(value.isInstance(JmmKind.INTEGER_LITERAL) || value.isInstance(JmmKind.BOOLEAN_LITERAL)) {
            if (definedVars.containsKey(varDecl.get("name")))
                definedVars.replace(varDecl.get("name"), value);
            else
                definedVars.put(varDecl.get("name"), value);
        }
        else definedVars.remove(varDecl.get("name"));
        return null;
    }

    private void replaceFunctionCall(JmmNode functionCall, Signature sig, String methodName) {
        var method = findMethod(table, sig, methodName, TypeUtils.with(table));
        var actualSig = method.signature();
        if(definedMethodReturns.containsKey(actualSig)) {
            JmmNode newNode;
            if(method.returnType().equals(TypeUtils.intType()))
                newNode = new JmmNodeImpl(JmmKind.INTEGER_LITERAL);
            else
                newNode = new JmmNodeImpl(JmmKind.BOOLEAN_LITERAL);
            newNode.put("value", definedMethodReturns.get(actualSig).get("value"));
            var parent = functionCall.getParent();
            var grandParent = parent.getParent();
            var replacement = functionCall.copyDeep(functionCall.getKind());
            for (var child : functionCall.getChildren()) {
                child.removeParent();
                replacement.add(child);
            }
            functionCall.replace(newNode);

            JmmNode statement = functionCall;
            while (statement.getParent() != null && !statement.getParent().isInstance(METHOD_DECL) && !statement.getParent().isInstance(BLOCK))
                statement = statement.getParent();

            if (statement.getParent() != null) {
                var methodBody = statement.getParent();
                methodBody.add(replacement, statement.getIndexOfSelf());
            }
            changed = true;
        }
    }

    //Copied from TYpeUtils... I don't like it, but I don't want to instantiate it either as it will fail with imported tables... TO REMOVE LATER
    //I can't guarantee your sanity bellow this point... I hate Java and its inheritance, man...
    private MethodSymbol findMethod(SymbolTable table, Signature sig, String methodName, TypeUtils tU) {
        var possibilities = table.getMethods(methodName);
        List<MethodSymbol> candidates = new ArrayList<>();

        for(var p : possibilities)
            if (isCompatible(p, sig, tU))
                candidates.add(p);

        //Get the best match. Long story short, this is my attempt at handling the filthy inheritance and its ambiguity
        //Like imagine, if we have a(Fruit f, Apple a), a(Fruit f, Fruit f) and then a(new Apple(), new Apple()), which should be called? Ah, Apple extends Fruit
        //And if we have a(Fruit f, Apple a), a(Apple a, Fruit f). Congratulations, different signatures but an ambiguity problem, I think.
        //Basically, we have to check which is more specific, if any
        //Do we even have to handle this kind of inheritance problem???
        if(!candidates.isEmpty())
            return mostSpecific(candidates, tU);

        return null;
    }

    private boolean isCompatible(MethodSymbol method, Signature sig, TypeUtils tU) {
        var candidateParams = method.signature().parameters();
        var reqParams = sig.parameters();

        if(candidateParams.size() != reqParams.size())
            return false;

        for(int i = 0; i < candidateParams.size(); i++)
            if(!tU.isAssignable(candidateParams.get(i), reqParams.get(i)))
                return false;

        return true;
    }

    private MethodSymbol mostSpecific(List<MethodSymbol> candidates, TypeUtils tU) {
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
            throw new RuntimeException("Ambiguous method call.\n");

        return best;
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

    public boolean madeChanges() {
        return changed;
    }
}
