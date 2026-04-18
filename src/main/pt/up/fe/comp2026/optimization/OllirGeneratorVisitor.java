package pt.up.fe.comp2026.optimization;

import pt.up.fe.comp.jmm.analysis.table.MethodSymbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.type.JmmType;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2026.ast.TypeUtils;
import pt.up.fe.comp2026.jmm.ast.JmmAttributes;

import java.util.stream.Collectors;

import static pt.up.fe.comp2026.jmm.ast.JmmKind.*;

/**
 * Generates OLLIR code from JmmNodes that are not expressions.
 */
public class OllirGeneratorVisitor extends AJmmVisitor<Void, String> {

    private static final String SPACE = " ";
    private static final String ASSIGN = ":=";
    private final String END_STMT = ";\n";
    private final String NL = "\n";
    private final String L_BRACKET = " {\n";
    private final String R_BRACKET = "}\n";

    private final SymbolTable table;
    private final TypeUtils types;
    private final OptUtils ollirTypes;
    private final OllirExprGeneratorVisitor exprVisitor;
    private MethodSymbol currentMethod;
    private int labelCounter = 0;

    public OllirGeneratorVisitor(SymbolTable table) {
        this.table = table;
        this.types = new TypeUtils(table);
        this.ollirTypes = new OptUtils(types);
        exprVisitor = new OllirExprGeneratorVisitor(table, ollirTypes);
        currentMethod = null;
    }

    @Override
    protected void buildVisitor() {
        addVisit(PROGRAM, this::visitProgram);
        addVisit(PACKAGE_DECL, this::visitPackageDecl);
        addVisit(CLASS_DECL, this::visitClass);
        addVisit(PARAM, this::visitParam);
        addVisit(METHOD_DECL, this::visitMethodDecl);
        addVisit(RETURN_STMT, this::visitReturn);
        addVisit(ASSIGN_STMT, this::visitAssignStmt);
        addVisit(ASSIGN_ARRAY_ELEMENT_STMT, this::visitAssignArrayElementStmt);
        addVisit(IF_STMT, this::visitIfStmt);
        addVisit(WHILE_STMT, this::visitWhileStmt);
        addVisit(EXPR_STMT, this::visitExprStmt);
        addVisit(BLOCK, this::visitBlock);
    }

    private String nextLabel(String prefix) {
        return prefix + labelCounter++;
    }

    private String visitParam(JmmNode param, Void unused) {
        return param.get("name") + ollirTypes.toOllirType(param.getObject("typeNode", JmmNode.class));
    }

    private String visitPackageDecl(JmmNode packageDecl, Void unused) {
        return "package " + String.join(".", packageDecl.getObjectAsList("path", String.class)) + ";\n";
    }

    private String visitAssignStmt(JmmNode node, Void unused) {
        var varName = node.get(JmmAttributes.ASSIGN_STMT.VAR);
        var rhs = exprVisitor.visit(node.getChild(0));
        var varType = getVarType(varName);
        var typeString = ollirTypes.toOllirType(varType);

        var code = new StringBuilder();
        code.append(rhs.getComputation());

        // field assignment uses putfield
        if (table.getField(varName).isPresent()) {
            code.append("putfield(this, ")
                    .append(ollirTypes.sanitizeId(varName)).append(typeString).append(", ")
                    .append(rhs.getCode()).append(").V")
                    .append(END_STMT);
            return code.toString();
        }

        // local variable or parameter assignment
        code.append(ollirTypes.sanitizeId(varName)).append(typeString)
                .append(SPACE).append(ASSIGN).append(typeString).append(SPACE)
                .append(rhs.getCode())
                .append(END_STMT);

        return code.toString();
    }

    private String visitAssignArrayElementStmt(JmmNode node, Void unused) {
        var varName = node.get("var");
        var varType = getVarType(varName);
        var typeString = ollirTypes.toOllirType(varType);

        // element type (strip one array dimension)
        var elemType = varType.asArray().itemType();
        var elemTypeString = ollirTypes.toOllirType(elemType);

        // visit indices
        var indices = node.getChildren(ARRAY_INDEX);
        var indexResults = indices.stream()
                .map(idx -> exprVisitor.visit(idx.getChild(0)))
                .toList();

        // visit rhs
        var rhs = exprVisitor.visit(node.getChildren().getLast());

        var code = new StringBuilder();
        indexResults.forEach(ir -> code.append(ir.getComputation()));
        code.append(rhs.getComputation());

        // A[i.i32].i32 :=.i32 val.i32;
        code.append(ollirTypes.sanitizeId(varName)).append(typeString)
                .append("[").append(indexResults.getFirst().getCode()).append("]")
                .append(elemTypeString)
                .append(SPACE).append(ASSIGN).append(elemTypeString).append(SPACE)
                .append(rhs.getCode())
                .append(END_STMT);

        return code.toString();
    }

    private String visitReturn(JmmNode node, Void unused) {
        var retType = currentMethod.returnType();
        var typeString = ollirTypes.toOllirType(retType);

        // void return
        if (retType.equals(TypeUtils.voidType())) {
            return "ret" + typeString + END_STMT;
        }

        // non-void return
        var expr = exprVisitor.visit(node.getChild(0));
        var code = new StringBuilder();
        code.append(expr.getComputation());
        code.append("ret").append(typeString).append(SPACE);
        code.append(expr.getCode());
        code.append(END_STMT);
        return code.toString();
    }

    private String visitIfStmt(JmmNode node, Void unused) {
        var thenLabel = nextLabel("then");
        var endLabel = nextLabel("endif");
        var hasElse = node.getChildren().size() == 3;
        var elseLabel = hasElse ? nextLabel("else") : endLabel;

        var cond = exprVisitor.visit(node.getChild(0));
        var code = new StringBuilder();

        code.append(cond.getComputation());
        code.append("if (").append(cond.getCode()).append(") goto ").append(thenLabel).append(END_STMT);

        // else body
        if (hasElse) {
            code.append(visit(node.getChild(2)));
        }
        code.append("goto ").append(endLabel).append(END_STMT);

        // then body
        code.append(thenLabel).append(":\n");
        code.append(visit(node.getChild(1)));

        // end
        code.append(endLabel).append(":\n");

        return code.toString();
    }

    private String visitWhileStmt(JmmNode node, Void unused) {
        var loopLabel = nextLabel("loop");
        var endLabel = nextLabel("endloop");

        var cond = exprVisitor.visit(node.getChild(0));
        var code = new StringBuilder();

        code.append(loopLabel).append(":\n");
        code.append(cond.getComputation());
        code.append("if (").append(cond.getCode()).append(") goto ").append(endLabel).append(END_STMT);
        code.append(visit(node.getChild(1)));
        code.append("goto ").append(loopLabel).append(END_STMT);
        code.append(endLabel).append(":\n");

        return code.toString();
    }

    private String visitExprStmt(JmmNode node, Void unused) {
        var expr = exprVisitor.visit(node.getChild(0));
        return expr.getComputation() + expr.getCode();
    }

    private String visitBlock(JmmNode node, Void unused) {
        return node.getChildren(STMT).stream()
                .map(this::visit)
                .collect(Collectors.joining());
    }

    private String visitMethodDecl(JmmNode node, Void unused) {
        var methodOpt = table.getMethod(TypeUtils.with(table).getMethodDeclSignature(node));
        if (methodOpt.isEmpty()) return "";
        currentMethod = methodOpt.get();

        StringBuilder code = new StringBuilder(".method ");

        // visibility modifier
        var visibility = node.getOptional("visibility");
        if (visibility.isPresent()) {
            code.append(visibility.get()).append(SPACE);
        }

        // static modifier
        if (node.getObject("isStatic", Boolean.class)) {
            code.append("static ");
        }

        // method name
        code.append(ollirTypes.sanitizeId(node.get("name")));

        // params
        var paramsCode = node.getChildren(PARAM).stream()
                .map(this::visit)
                .collect(Collectors.joining(", "));
        code.append("(").append(paramsCode).append(")");

        // return type
        code.append(ollirTypes.toOllirType(currentMethod.returnType()));
        code.append(L_BRACKET);

        // local variable declarations
        for (var local : currentMethod.localVariables()) {
            code.append(SPACE).append(SPACE)
                    .append(ollirTypes.sanitizeId(local.name()))
                    .append(ollirTypes.toOllirType(local.type()))
                    .append(END_STMT);
        }

        // statements
        for (var stmt : node.getChildren(STMT)) {
            var stmtCode = visit(stmt);
            if (stmtCode != null && !stmtCode.isEmpty()) {
                code.append(SPACE).append(SPACE).append(stmtCode);
            }
        }

        code.append(R_BRACKET);
        code.append(NL);
        currentMethod = null;
        return code.toString();
    }

    private String visitClass(JmmNode node, Void unused) {
        StringBuilder code = new StringBuilder();

        code.append(NL);
        code.append(table.getClassName());

        // extends
        var superClass = table.getSuper();
        if (superClass != null && !superClass.equals("Object")) {
            code.append(" extends ").append(superClass);
        }

        code.append(L_BRACKET);
        code.append(NL);

        // field declarations
        for (var field : table.getFields()) {
            code.append(".field ")
                    .append(ollirTypes.sanitizeId(field.name()))
                    .append(ollirTypes.toOllirType(field.type()))
                    .append(END_STMT);
        }
        code.append(NL);

        code.append(buildConstructor());
        code.append(NL);

        for (var child : node.getChildren(METHOD_DECL)) {
            code.append(visit(child));
        }

        code.append(R_BRACKET);
        return code.toString();
    }

    private String buildConstructor() {
        return """
                .construct %s().V {
                    invokespecial(this, "<init>").V;
                }
                """.formatted(table.getClassName());
    }

    private String visitProgram(JmmNode node, Void unused) {
        StringBuilder code = new StringBuilder();
        node.getChildren().stream().map(this::visit).forEach(code::append);
        return code.toString();
    }

    private JmmType getVarType(String varName) {
        var local = currentMethod.getLocalVariable(varName);
        if (local.isPresent()) return local.get().type();

        var param = currentMethod.getParameter(varName);
        if (param.isPresent()) return param.get().type();

        var field = table.getField(varName);
        if (field.isPresent()) return field.get().type();

        throw new RuntimeException("Can't find type for variable '" + varName + "'");
    }
}