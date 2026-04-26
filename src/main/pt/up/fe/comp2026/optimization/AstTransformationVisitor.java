package pt.up.fe.comp2026.optimization;

import org.stringtemplate.v4.ST;
import pt.up.fe.comp.jmm.analysis.table.MethodSymbol;
import pt.up.fe.comp.jmm.analysis.table.Signature;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.reflection.Importer;
import pt.up.fe.comp.jmm.ast.*;
import pt.up.fe.comp2026.ast.TypeUtils;
import pt.up.fe.comp2026.jmm.ast.JmmKind;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static pt.up.fe.comp2026.jmm.ast.JmmKind.EXPR;
import static pt.up.fe.comp2026.jmm.ast.JmmKind.STMT;

public class AstTransformationVisitor extends PostorderJmmVisitor<Void, Void> {
    final private SymbolTable table;

    public AstTransformationVisitor(SymbolTable table) {
        this.table = table;
    }
    @Override
    protected void buildVisitor() {
        addVisit(JmmKind.FOR_STMT, this::visitFor);
        addVisit(JmmKind.DO_WHILE_STMT, this::visitDoWhile);
        addVisit(JmmKind.PARENTHESIS, this::visitParenthesis);
        addVisit(JmmKind.BLOCK, this::visitBlock);
        addVisit(JmmKind.METHOD_DECL, this::visitMethodDecl);
        addVisit(JmmKind.IMPLICIT_THIS_FUNCTION_CALL, this::visitImplicitThisCall);
        addVisit(JmmKind.FUNCTION_CALL, this::visitFunctionCall);
        addVisit(JmmKind.UNARY_EXPR, this::visitUnaryOp);
        setDefaultVisit(this::defaultVisit);
    }

    private Void defaultVisit(JmmNode node, Void unused) {
        return null;
    }

    //Transform a for in a while loop
    private Void visitFor(JmmNode forStmt, Void unused) {
        var assignFor = forStmt.getChildren(JmmKind.ASSIGN_FOR);
        if (!assignFor.isEmpty()) {
            var initNode = assignFor.getFirst();
            var expr = initNode.getChild(0);
            expr.removeParent();

            var newNode = new JmmNodeImpl(JmmKind.ASSIGN_STMT);
            newNode.put("var", initNode.get("var"));
            newNode.add(expr);

            forStmt.getParent().add(newNode, forStmt.getIndexOfSelf());
        }

        var whileNode = new JmmNodeImpl(JmmKind.WHILE_STMT);

        var eval = forStmt.getChildren(JmmKind.EXPR);
        if (!eval.isEmpty()) {
            var cond = eval.getFirst();
            cond.removeParent();
            whileNode.add(cond);
        } else {
            var trueLiteral = new JmmNodeImpl(JmmKind.BOOLEAN_LITERAL);
            trueLiteral.put("value", "true");
            whileNode.add(trueLiteral);
        }

        var whileBody = new JmmNodeImpl(JmmKind.BLOCK);
        var bodyStmts = forStmt.getChildren(JmmKind.STMT);
        for (var s : bodyStmts) {
            s.removeParent();
            whileBody.add(s);
        }

        var assignFor2 = forStmt.getChildren(JmmKind.ASSIGN_FOR2);
        if (!assignFor2.isEmpty()) {
            var updateInit = assignFor2.getFirst();
            var updateExpr = updateInit.getChild(0);
            updateExpr.removeParent();

            var updateNode = new JmmNodeImpl(JmmKind.ASSIGN_STMT);
            updateNode.put("var", updateInit.get("var"));
            updateNode.add(updateExpr);

            whileBody.add(updateNode);
        }

        whileNode.add(whileBody);
        forStmt.replace(whileNode);
        return null;
    }

    //Transform a do while in a while loop
    private Void visitDoWhile(JmmNode doWhileStmt, Void unused) {
        var stmt = doWhileStmt.getChild(0);
        stmt.removeParent();
        var expr = doWhileStmt.getChild(1);
        expr.removeParent();
        List<JmmNode> stmts = stmt.getChildren();

        var parent = doWhileStmt.getParent();
        for(var st : stmts)
            parent.add(st, doWhileStmt.getIndexOfSelf());

        var newNode = new JmmNodeImpl(JmmKind.WHILE_STMT);
        newNode.add(expr);
        newNode.add(stmt);
        doWhileStmt.replace(newNode);
        return null;
    }

    private Void visitParenthesis(JmmNode paren, Void unused) {
        if(paren.getChildren().isEmpty()) {
            paren.removeParent();
            return null;
        }
        var child = paren.getChild(0);
        child.removeParent();
        paren.replace(child);
        return null;
    }

    private Void visitBlock(JmmNode block, Void unused) {
        if(block.getChildren().isEmpty()) {
            block.getParent().removeChild(block);
            block.removeParent();
            return null;
        }
        var children = block.getChildren();
        for(var child : children) {
            child.removeParent();
            block.getParent().add(child, block.getIndexOfSelf());
        }
        block.getParent().removeChild(block);
        block.removeParent();
        return null;
    }

    private Void visitMethodDecl(JmmNode methodDecl, Void unused) {
        var isRet = methodDecl.getChildren().getLast().isInstance(JmmKind.RETURN_STMT);
        if(!isRet)
            methodDecl.add(new JmmNodeImpl(JmmKind.RETURN_STMT));
        return null;
    }

    private Void visitUnaryOp(JmmNode unaryOp, Void unused) {
        var op = unaryOp.get("op");
        var expr = unaryOp.getChild(0);
        switch (op) {
            case "+" -> {  //Useless op
                expr.removeParent();
                unaryOp.replace(expr);
            }
            case "-" -> {
                var newNode = new JmmNodeImpl(JmmKind.BINARY_EXPR);
                var zero = new JmmNodeImpl(JmmKind.INTEGER_LITERAL);
                newNode.put("op", "-");
                zero.put("value", "0");
                newNode.add(zero);
                expr.removeParent();
                newNode.add(expr);
                unaryOp.replace(newNode);
            }
            case "++" -> {
                //Not sure of how complete this actually is. Not sure if it handles (or we even have to handle) fields
                //Same story for the next one
                var assignNode = new JmmNodeImpl(JmmKind.ASSIGN_STMT);
                var assignOp = new JmmNodeImpl(JmmKind.BINARY_EXPR);
                var one = new JmmNodeImpl(JmmKind.INTEGER_LITERAL);
                one.put("value", "1");
                assignOp.put("op", "+");
                expr.removeParent();
                var exprCopy = expr.copy();
                assignOp.add(exprCopy);
                assignOp.add(one);
                assignNode.put("var", expr.get("name"));
                assignNode.add(assignOp);
                var parentStmt = unaryOp.getAncestor(STMT).get();
                parentStmt.getParent().add(assignNode, parentStmt.getIndexOfSelf());
                unaryOp.replace(expr);
            }
            case "--" -> {
                var assignNode = new JmmNodeImpl(JmmKind.ASSIGN_STMT);
                var assignOp = new JmmNodeImpl(JmmKind.BINARY_EXPR);
                var one = new JmmNodeImpl(JmmKind.INTEGER_LITERAL);
                one.put("value", "1");
                assignOp.put("op", "-");
                expr.removeParent();
                var exprCopy = expr.copy();
                assignOp.add(exprCopy);
                assignOp.add(one);
                assignNode.put("var", expr.get("name"));
                assignNode.add(assignOp);
                var parentStmt = unaryOp.getAncestor(STMT).get();
                parentStmt.getParent().add(assignNode, parentStmt.getIndexOfSelf());
                unaryOp.replace(expr);
            }
        }
        return null;
    }

    //If a static method is called with a class instantiation, we should change it to a static class reference
    //Largely copied from LocalCallsSemanticPass... I genuinely need to simplify this thing...
    private Void visitImplicitThisCall(JmmNode call, Void unused) {
        var methodName = call.get("func");
        var params = call.getChildren(EXPR);
        var thisNode = new JmmNodeImpl(JmmKind.THIS);
        var newNode = new JmmNodeImpl(JmmKind.FUNCTION_CALL);
        newNode.put("func", methodName);
        newNode.add(thisNode);
        for(var param : params) {
            param.removeParent();
            newNode.add(param);
        }
        call.replace(newNode);
        call.removeParent();
        return visitFunctionCall(newNode, unused);
    }

    private Void visitFunctionCall(JmmNode functionCall, Void  unused) {
        var tU = TypeUtils.with(table);
        var methodName = functionCall.get("func");
        var target = functionCall.getChild(0);
        var params = functionCall.getChildren(EXPR).subList(1, functionCall.getChildren(EXPR).toArray().length).stream().map(p -> TypeUtils.with(table).getExprType(p)).toList();
        var sig = new Signature(methodName, params);

        var called = TypeUtils.with(table).getExprType(target);
        var isStatic = called.asClass().staticRef();

        if(isStatic)
            return null;

        Optional<SymbolTable> curTable = Optional.empty();
        if(called.asClass().fullyQualifiedName().equals(table.getFullyQualifiedName()))
            curTable = Optional.of(table);
        else
        if(Importer.fromThisClassPath().isImplicitImport(called.asClass().fullyQualifiedName()))
            curTable = Importer.fromThisClassPath().tryImplicitImport(called.asClass().fullyQualifiedName());
        else if(Importer.fromThisClassPath().inClassPath(called.asClass().fullyQualifiedName()))
            curTable = Importer.fromThisClassPath().getSymbolTableOf(called.asClass().fullyQualifiedName());

        if(curTable.isEmpty())
            throw new RuntimeException("Can't find any method corresponding to " + methodName);

        var fTable = curTable.get();
        var method = findMethod(fTable, sig, methodName, tU);
        if(method != null && method.isStatic()) {
            var newNode = new JmmNodeImpl(JmmKind.VAR_REF_EXPR);
            newNode.put("name", curTable.getClass().getCanonicalName());
            target.replace(newNode);
            target.removeParent();
        }

        return null;
    }

    //Copied from TYpeUtils... I don't like it, but I don't want to instantiate it either as it will fail with imported tables...
    //I can't guarantee your sanity bellow this point... I hate Java and its inheritance, man...
    private MethodSymbol findMethod(SymbolTable table, Signature sig, String methodName, TypeUtils tU) {
        SymbolTable curTable = table;

        while(curTable != null) {
            var possibilities = curTable.getMethods(methodName);
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

            String fqn = curTable.getSuperFullyQualifiedName();
            if(fqn == null)
                break;

            var imported = Importer.fromThisClassPath().getSymbolTableOf(fqn);
            if(imported.isEmpty())
                imported = Importer.fromThisClassPath().tryImplicitImport(fqn);

            curTable = imported.orElse(null);
        }

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

}
