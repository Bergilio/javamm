package pt.up.fe.comp2026.optimization;

import pt.up.fe.comp.jmm.analysis.table.MethodSymbol;
import pt.up.fe.comp.jmm.analysis.table.Signature;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.reflection.Importer;
import pt.up.fe.comp.jmm.analysis.table.type.JmmType;
import pt.up.fe.comp.jmm.analysis.table.type.impls.JmmArrayType;
import pt.up.fe.comp.jmm.analysis.table.type.impls.JmmPrimitiveType;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2026.ast.TypeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static pt.up.fe.comp2026.jmm.ast.JmmKind.*;

/**
 * Generates OLLIR code from JmmNodes that are expressions.
 */
public class OllirExprGeneratorVisitor extends AJmmVisitor<Void, OllirExprResult> {

    private static final String SPACE = " ";
    private static final String ASSIGN = ":=";
    private final String END_STMT = ";\n";
    private static final String TAB = "\t";

    private final SymbolTable table;

    private final TypeUtils types;
    private final OptUtils ollirTypes;
    private MethodSymbol currentMethod;


    public OllirExprGeneratorVisitor(SymbolTable table, OptUtils ollirTypes) {
        this.table = table;
        this.types = new TypeUtils(table);
        this.ollirTypes = ollirTypes; // We need to pass ollirTypes, to ensure labels are unique
    }

    public void setCurrentMethod(MethodSymbol currentMethod) {
        this.currentMethod = currentMethod;
    }

    @Override
    protected void buildVisitor() {
        addVisit(VAR_REF_EXPR, this::visitVarRef);
        addVisit(BINARY_EXPR, this::visitBinExpr);
        addVisit(INTEGER_LITERAL, this::visitInteger);
        addVisit(ARRAY_ACCESS, this::visitArrayAccess);
        addVisit(ARRAY_ALLOC, this::visitArrayAlloc);
        addVisit(LENGTH_OPERATOR, this::visitLengthOp);
        addVisit(FUNCTION_CALL, this::visitFunctionCall);
        addVisit(THIS, this::visitThis);
        addVisit(BOOLEAN_LITERAL, this::visitBoolean);
        addVisit(UNARY_EXPR, this::visitUnaryExpr);
        addVisit(CONSTRUCTOR, this::visitConstructor);
        addVisit(FIELD_ACCESS, this::visitFieldAccess);
        addVisit(ARRAY_ALLOC_INIT, this::visitArrayAllocInit);
    }

    private OllirExprResult visitInteger(JmmNode node, Void unused) {
        var intType = TypeUtils.intType();
        String ollirIntType = ollirTypes.toOllirType(intType);
        String code = node.get("value") + ollirIntType;
        return new OllirExprResult(code);
    }


    private OllirExprResult visitBinExpr(JmmNode node, Void unused) {
        var op = node.get("op");

        if (op.equals("&&") || op.equals("||")) {
            return visitShortCircuitExpr(node, op);
        }

        var lhs = visit(node.getChild(0));
        var rhs = visit(node.getChild(1));

        StringBuilder computation = new StringBuilder();

        // code to compute the children
        computation.append(lhs.getComputation());
        computation.append(rhs.getComputation());

        // code to compute self
        JmmType resType = types.getExprType(node);
        String resOllirType = ollirTypes.toOllirType(resType);
        String code = ollirTypes.nextTemp() + resOllirType;

        computation.append(TAB).append(code).append(SPACE)
                .append(ASSIGN).append(resOllirType).append(SPACE)
                .append(lhs.getCode()).append(SPACE);

        JmmType type = types.getExprType(node);
        computation.append(op).append(ollirTypes.toOllirType(type)).append(SPACE)
                .append(rhs.getCode()).append(END_STMT);

        return new OllirExprResult(code, computation);
    }

    private OllirExprResult visitShortCircuitExpr(JmmNode node, String op) {
        var lhs = visit(node.getChild(0));
        var rhs = visit(node.getChild(1));

        var trueLabel = ollirTypes.nextTemp(op.equals("&&") ? "andTrue" : "orTrue");
        var endLabel = ollirTypes.nextTemp(op.equals("&&") ? "andEnd" : "orEnd");
        var temp = ollirTypes.nextTemp() + ".bool";

        var computation = new StringBuilder();
        computation.append(lhs.getComputation());
        computation.append("if (").append(lhs.getCode()).append(") goto ").append(trueLabel).append(END_STMT);

        if (op.equals("&&")) {
            computation.append(temp).append(" :=.bool 0.bool").append(END_STMT);
            computation.append("goto ").append(endLabel).append(END_STMT);
            computation.append(trueLabel).append(":\n");
            computation.append(rhs.getComputation());
            computation.append(temp).append(" :=.bool ").append(rhs.getCode()).append(END_STMT);
        } else {
            computation.append(rhs.getComputation());
            computation.append(temp).append(" :=.bool ").append(rhs.getCode()).append(END_STMT);
            computation.append("goto ").append(endLabel).append(END_STMT);
            computation.append(trueLabel).append(":\n");
            computation.append(temp).append(" :=.bool 1.bool").append(END_STMT);
        }
        computation.append(endLabel).append(":\n");

        return new OllirExprResult(temp, computation);
    }

    private OllirExprResult visitArrayAccess(JmmNode node, Void unused) {
        var arrayExpr = visit(node.getChild(0));
        var arrayExprType = types.getExprType(node.getChild(0));

        var accessType = types.arrayAccessType(node);
        var accessResOllirType = ollirTypes.toOllirType(accessType);


        var indices = node.getChildren(ARRAY_INDEX);
        var indexResults = indices.stream()
                .map(idx -> this.visit(idx.getChild(0)))
                .toList();

        var computation = new StringBuilder();
        var code = "";
        var prev = arrayExpr.getCode();


        computation.append(arrayExpr.getComputation());
        indexResults.forEach(ir -> computation.append(ir.getComputation()));

        for(int i = 0; i < indexResults.size() - 1; i++) {
            var index = indexResults.get(i);
            var temp = ollirTypes.nextTemp();
            var interAccessType = ollirTypes.toOllirType(new JmmArrayType(arrayExprType.asArray().itemType(),
                    arrayExprType.asArray().dimension() - i - 1));
            computation.append(TAB).append(temp).append(interAccessType).append(SPACE).append(ASSIGN).append(interAccessType).append(SPACE)
                    .append(prev).append('[').append(index.getCode()).append(']').append(interAccessType).append(END_STMT);
            prev = temp;
        }

        code = ollirTypes.nextTemp() + accessResOllirType;
        computation.append(TAB).append(code).append(SPACE).append(ASSIGN).append(accessResOllirType).append(SPACE).append(prev)
        .append('[').append(indexResults.getLast().getCode()).append(']');
        computation.append(accessResOllirType).append(END_STMT);

        return new OllirExprResult(code, computation);
    }

    private OllirExprResult visitVarRef(JmmNode node, Void unused) {
        var id = ollirTypes.sanitizeId(node.get("name"));
        JmmType type = types.getExprType(node);
        String ollirType = ollirTypes.toOllirType(type);

        if(currentMethod.getParameter(node.get("name")).isPresent() || currentMethod.getLocalVariable(node.get("name")).isPresent())
            return new OllirExprResult(id + ollirType, "");

        // if it's a class field, use getfield
        if (table.getField(node.get("name")).isPresent()) {
            var computation = new StringBuilder();
            var temp = ollirTypes.nextTemp() + ollirType;
            computation.append(TAB).append(temp).append(SPACE).append(ASSIGN).append(ollirType).append(SPACE)
                    .append("getfield(this, ").append(id).append(ollirType).append(")").append(ollirType).append(END_STMT);
            return new OllirExprResult(temp, computation);
        }

        return new OllirExprResult(id + ollirType);
    }

    private OllirExprResult visitArrayAlloc(JmmNode node, Void unused) {
        var arrayType = types.getExprType(node);
        var ollirType = ollirTypes.toOllirType(arrayType);

        var indices = node.getChildren(ARRAY_INDEX);
        var indexResults = indices.stream()
                .map(idx -> this.visit(idx.getChild(0)))
                .toList();

        var computation = new StringBuilder();

        indexResults.forEach(ir -> computation.append(ir.getComputation()));

        var code = ollirTypes.nextTemp() + ollirType;
        computation.append(TAB).append(code).append(SPACE).append(ASSIGN).append(ollirType).append(SPACE).append("new(array");
        indexResults.forEach(ir -> computation.append(", ").append(ir.getCode()));
        computation.append(")").append(ollirType).append(END_STMT);

        return new OllirExprResult(code, computation);
    }

    private OllirExprResult visitLengthOp(JmmNode node, Void unused) {
        var arrayRef =  visit(node.getChild(0));

        var computation = new StringBuilder();
        var id = ollirTypes.nextTemp();
        computation.append(arrayRef.getComputation());
        computation.append(TAB).append(id).append(".i32").append(SPACE).append(ASSIGN).append(".i32").append(SPACE).append("arraylength(").append(arrayRef.getCode()).append(").i32").append(END_STMT);
        var code = id + ".i32";
        return new OllirExprResult(code, computation);
    }

    private OllirExprResult visitFunctionCall(JmmNode node, Void unused) {
        var accessed = node.getChild(0);
        var args = node.getChildren();
        args.removeFirst();
        var methodType = types.getMethodCallTypeFC(node);

        var tU = TypeUtils.with(table);
        var methodName = node.get("func");
        var params = node.getChildren(EXPR).subList(1, node.getChildren(EXPR).toArray().length).stream().map(p -> TypeUtils.with(table).getExprType(p)).toList();
        var sig = new Signature(methodName, params);

        var called = TypeUtils.with(table).getExprType(accessed);
        var isStaticRef = called.asClass().staticRef();
        var fqn = called.asClass().fullyQualifiedName();
        SymbolTable tableTemp;

        if(!fqn.equals(table.getFullyQualifiedName()))
            tableTemp = Importer.fromThisClassPath().getSymbolTableOf(fqn).orElseThrow();
        else
            tableTemp = table;

        var method = findMethod(tableTemp, sig, methodName, isStaticRef, tU, node);
        var paramTypes = method.signature().parameters();

        var accessedRes = visit(accessed);
        var argsRes = args.stream().map(this::visit).toList();

        var computation = new StringBuilder();
        computation.append(accessedRes.getComputation());
        argsRes.forEach(ir -> computation.append(ir.getComputation()));

        var castArgs = new ArrayList<String>();
        for(int i = 0; i < argsRes.size(); i++) {
            var argType = ollirTypes.toOllirType(paramTypes.get(i));
            var temp = ollirTypes.nextTemp() + argType;
            computation.append(temp).append(' ').append(ASSIGN)
                    .append(argType).append(' ').append(argsRes.get(i).getCode()).append(END_STMT);
            castArgs.add(temp);
        }

        String code = "";
        if(methodType != JmmPrimitiveType.VOID) {
            code = ollirTypes.nextTemp() + ollirTypes.toOllirType(methodType);
            computation.append(TAB).append(code).append(SPACE).append(ASSIGN).append(ollirTypes.toOllirType(methodType)).append(SPACE);
        }

        var invokeType = isStaticRef ? "invokestatic(" + called.asClass().name() : "invokevirtual(" + accessedRes.getCode();

        computation.append(invokeType).append(", \"").append(ollirTypes.sanitizeId(node.get("func"))).append("\"");
        castArgs.forEach(ir -> computation.append(", ").append(ir));
        computation.append(')').append(ollirTypes.toOllirType(methodType)).append(END_STMT);

        return new OllirExprResult(code, computation);
    }

    private OllirExprResult visitThis(JmmNode node, Void unused) {
        return new OllirExprResult("this" + ollirTypes.toOllirType(types.getExprType(node)));
    }

    private OllirExprResult visitBoolean(JmmNode node, Void unused) {
        String value = node.get("value").equals("true") ? "1" : "0";
        return new OllirExprResult(value + ollirTypes.toOllirType(TypeUtils.boolType()));
    }

    private OllirExprResult visitUnaryExpr(JmmNode node, Void unused) {
        var op = node.get("op");
        var operand = visit(node.getChild(0));
        JmmType resType = types.getExprType(node);
        String resOllirType = ollirTypes.toOllirType(resType);

        var computation = new StringBuilder();
        computation.append(operand.getComputation());

        String code = ollirTypes.nextTemp() + resOllirType;
        computation.append(TAB).append(code).append(SPACE).append(ASSIGN).append(resOllirType).append(SPACE)
                .append(op).append(resOllirType).append(SPACE)
                .append(operand.getCode()).append(END_STMT);

        return new OllirExprResult(code, computation);
    }

    private OllirExprResult visitConstructor(JmmNode node, Void unused) {
        var className = node.get("classType");
        var classOllirType = "." + className;
        var args = node.getChildren(EXPR);
        var argsRes = args.stream().map(this::visit).toList();

        var computation = new StringBuilder();
        argsRes.forEach(ir -> computation.append(ir.getComputation()));

        String temp = ollirTypes.nextTemp() + classOllirType;
        computation.append(TAB).append(temp).append(SPACE).append(ASSIGN).append(classOllirType).append(SPACE)
                .append("new(").append(className).append(")").append(classOllirType).append(END_STMT);

        computation.append("invokespecial(").append(temp).append(", \"<init>\"");
        argsRes.forEach(ir -> computation.append(", ").append(ir.getCode()));
        computation.append(").V").append(END_STMT);

        return new OllirExprResult(temp, computation);
    }

    private OllirExprResult visitFieldAccess(JmmNode node, Void unused) {
        var obj = visit(node.getChild(0));
        var fieldName = node.get("name");
        JmmType type = types.getExprType(node);
        String ollirType = ollirTypes.toOllirType(type);

        var computation = new StringBuilder();
        computation.append(obj.getComputation());
        var temp = ollirTypes.nextTemp() + ollirType;
        computation.append(temp).append(SPACE).append(ASSIGN).append(ollirType).append(SPACE)
                .append("getfield(").append(obj.getCode()).append(", ")
                .append(ollirTypes.sanitizeId(fieldName)).append(ollirType).append(")")
                .append(ollirType).append(END_STMT);
        return new OllirExprResult(temp, computation);
    }

    private OllirExprResult visitArrayAllocInit(JmmNode node, Void unused) {
        var arrayType = types.getExprType(node);
        var ollirType = ollirTypes.toOllirType(arrayType);

        var computation = new StringBuilder();

        var initVals = node.getChildren(EXPR).stream().map(this::visit).toList();
        initVals.forEach(ir -> computation.append(ir.getComputation()));

        var code = ollirTypes.nextTemp() + ollirType;
        computation.append(TAB).append(code).append(SPACE).append(ASSIGN).append(ollirType).append(SPACE).
                append("new(array, ").append(initVals.size()).append(".i32").append(")").append(ollirType).append(END_STMT);

        String accessType;
        var array = arrayType.asArray();
        if(array.dimension() == 1)
            accessType = ollirTypes.toOllirType(array.itemType());
        else
            accessType = "array".repeat(array.dimension() - 1) + ollirTypes.toOllirType(array.itemType());
        for(var i = 0; i < initVals.size(); i++)
            computation.append(TAB).append(code).append('[').append(i).append(".i32").append(']').append(accessType)
                    .append(SPACE).append(ASSIGN).append(accessType).append(SPACE).append(initVals.get(i).getCode()).append(END_STMT);

        return new OllirExprResult(code, computation);
    }

    //Copied from TYpeUtils... I don't like it, but I don't want to instantiate it either as it will fail with imported tables...
    //I can't guarantee your sanity bellow this point... I hate Java and its inheritance, man...
    private MethodSymbol findMethod(SymbolTable table, Signature sig, String methodName, boolean isStaticCall, TypeUtils tU, JmmNode call) {
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

    private MethodSymbol mostSpecific(List<MethodSymbol> candidates, TypeUtils tU, JmmNode call) {
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
