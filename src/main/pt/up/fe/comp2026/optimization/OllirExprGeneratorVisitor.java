package pt.up.fe.comp2026.optimization;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.type.JmmType;
import pt.up.fe.comp.jmm.analysis.table.type.impls.JmmPrimitiveType;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2026.ast.TypeUtils;

import java.util.stream.Collectors;

import static pt.up.fe.comp2026.jmm.ast.JmmKind.*;

/**
 * Generates OLLIR code from JmmNodes that are expressions.
 */
public class OllirExprGeneratorVisitor extends AJmmVisitor<Void, OllirExprResult> {

    private static final String SPACE = " ";
    private static final String ASSIGN = ":=";
    private final String END_STMT = ";\n";

    private final SymbolTable table;

    private final TypeUtils types;
    private final OptUtils ollirTypes;


    public OllirExprGeneratorVisitor(SymbolTable table, OptUtils ollirTypes) {
        this.table = table;
        this.types = new TypeUtils(table);
        this.ollirTypes = ollirTypes; // We need to pass ollirTypes, to ensure labels are unique
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

        computation.append(code).append(SPACE)
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

        var accessType = types.arrayAccessType(node);
        var accessResOllirType = ollirTypes.toOllirType(accessType);


        var indices = node.getChildren(ARRAY_INDEX);
        var indexResults = indices.stream()
                .map(idx -> this.visit(idx.getChild(0)))
                .toList();

        var computation = new StringBuilder();
        var code = new String();

        computation.append(arrayExpr.getComputation());
        indexResults.forEach(ir -> computation.append(ir.getComputation()));

        code = ollirTypes.nextTemp() + accessResOllirType;
        computation.append(code).append(SPACE).append(ASSIGN).append(accessResOllirType).append(SPACE).append(arrayExpr.getCode());
        indexResults.forEach(ir -> computation.append('[').append(ir.getCode()).append(']'));
        computation.append(accessResOllirType).append(END_STMT);

        return new OllirExprResult(code, computation);
    }

    private OllirExprResult visitVarRef(JmmNode node, Void unused) {
        var id = ollirTypes.sanitizeId(node.get("name"));
        JmmType type = types.getExprType(node);
        String ollirType = ollirTypes.toOllirType(type);

        // if it's a class field, use getfield
        if (table.getField(node.get("name")).isPresent()) {
            var computation = new StringBuilder();
            var temp = ollirTypes.nextTemp() + ollirType;
            computation.append(temp).append(SPACE).append(ASSIGN).append(ollirType).append(SPACE)
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
        var code = new StringBuilder();

        indexResults.forEach(ir -> computation.append(ir.getComputation()));

        code.append("new(array");
        indexResults.forEach(ir -> code.append(", ").append(ir.getCode()));
        code.append(")").append(ollirType);

        return new OllirExprResult(code.toString(), computation);
    }

    private OllirExprResult visitLengthOp(JmmNode node, Void unused) {
        var arrayRef =  visit(node.getChild(0));

        var computation = new StringBuilder();
        var id = ollirTypes.nextTemp();
        computation.append(id).append(".i32").append(SPACE).append(ASSIGN).append(".i32").append(SPACE).append("arraylength(").append(arrayRef.getCode()).append(").i32").append(END_STMT);
        var code = id + ".i32";
        return new OllirExprResult(code, computation);
    }

    private OllirExprResult visitFunctionCall(JmmNode node, Void unused) {
        var accessed = node.getChild(0);
        var args = node.getChildren();
        args.removeFirst();
        var methodType = types.getMethodCallTypeFC(node);

        var called = TypeUtils.with(table).getExprType(accessed);
        var isStaticRef = called.asClass().staticRef();

        var accessedRes = visit(accessed);
        var argsRes = args.stream().map(this::visit).toList();

        var computation = new StringBuilder();
        computation.append(accessedRes.getComputation());
        argsRes.forEach(ir -> computation.append(ir.getComputation()));
        String code = "";
        if(methodType != JmmPrimitiveType.VOID) {
            code = ollirTypes.nextTemp() + ollirTypes.toOllirType(methodType);
            computation.append(code).append(SPACE).append(ASSIGN).append(ollirTypes.toOllirType(methodType)).append(SPACE);
        }

        var invokeType = isStaticRef ? "invokestatic(" + called.asClass().name() : "invokevirtual(" + accessedRes.getCode();

        computation.append(invokeType).append(", \"").append(ollirTypes.sanitizeId(node.get("func"))).append("\"");
        argsRes.forEach(ir -> computation.append(", ").append(ir.getCode()));
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
        computation.append(code).append(SPACE).append(ASSIGN).append(resOllirType).append(SPACE)
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
        computation.append(temp).append(SPACE).append(ASSIGN).append(classOllirType).append(SPACE)
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
}
