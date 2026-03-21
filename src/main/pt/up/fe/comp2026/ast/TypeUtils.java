package pt.up.fe.comp2026.ast;

import pt.up.fe.comp.jmm.analysis.table.MethodSymbol;
import pt.up.fe.comp.jmm.analysis.table.Signature;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.type.impls.JmmPrimitiveType;
import pt.up.fe.comp.jmm.analysis.table.type.JmmType;
import pt.up.fe.comp.jmm.analysis.table.type.impls.JmmArrayType;
import pt.up.fe.comp.jmm.analysis.table.type.impls.JmmClassType;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2026.jmm.ast.JmmKind;
import pt.up.fe.comp2026.symboltable.JmmSymbolTable;
import pt.up.fe.specs.util.SpecsCheck;
import pt.up.fe.specs.util.exceptions.NotImplementedException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static pt.up.fe.comp2026.jmm.ast.JmmKind.*;


/**
 * Utility methods regarding types.
 */
public class TypeUtils {


    private final JmmSymbolTable table;

    public TypeUtils(SymbolTable table) {
        this.table = (JmmSymbolTable) table;
    }

    public static TypeUtils with(SymbolTable table) {
        return new TypeUtils(table);
    }

    public static JmmPrimitiveType intType() {
        return JmmPrimitiveType.INT;
    }

    public static JmmPrimitiveType boolType() {
        return JmmPrimitiveType.BOOLEAN;
    }

    public static JmmPrimitiveType voidType() {
        return JmmPrimitiveType.VOID;
    }

    public JmmType convertType(JmmNode typeNode) {
        assert (JmmKind.check(typeNode, BASE_TYPE, CLASS_TYPE, ARRAY_TYPE));

        System.out.println("[TODO] TypeUtils.convertType(): Implement for classes and arrays");
        var name = typeNode.get("name");
        return JmmPrimitiveType.fromString(name).orElseThrow();
    }

    /**
     * Gets the {@link JmmType} of an arbitrary expression.
     *
     * @param expr
     * @return
     */
    public JmmType getExprType(JmmNode expr) {
        return switch (expr.getKind()) {
            case INTEGER_LITERAL -> intType();
            case BINARY_EXPR -> getBinExprType(expr);
            case VAR_REF_EXPR -> getVarExprType(expr);
            case BOOLEAN_LITERAL -> boolType();
            default ->
                    throw new UnsupportedOperationException("Can't compute type for expression kind '" + expr.getKind() + "'");
        };
    }

    private JmmType getTypeParam(JmmNode param) {
        var type = param.getChild(0).get("name");
        return switch (type) {
            case "int" -> intType();
            case "boolean" -> boolType();
            default ->
                    throw new RuntimeException("Can't compute type for param type '" + type + "'");
        };
    }

    public Signature getMethodDeclSignature(JmmNode methodDecl) {
        // Ensure given node is a MethodDecl
        METHOD_DECL.check(methodDecl);

        // Get name of the method
        var methodName = methodDecl.get("name");

        // System.out.println("[TODO] TypeUtils.getMethodDeclSignature(): Supporting only methods with a single parameter that is an int, needs to be expanded");
        var children = methodDecl.getChildren(PARAM);
        var params = children.stream().map(this::getTypeParam).toList();

        // Create method signature with method name and types of parameters
        return new Signature(methodName, params);
    }


    public JmmType getMethodType(JmmNode methodDecl) {
        METHOD_DECL.check(methodDecl);
        var type = methodDecl.getChild(0).get("name");
        return switch (type) {
            case "int" -> intType();
            case "boolean" -> boolType();
            case "void" -> voidType();
            default ->
                    throw new RuntimeException("Can't compute type for method '" + methodDecl.get("name") + "'");
        };
    }

    private JmmType getBinExprType(JmmNode binaryExpr) {

        // Get operator
        String operator = binaryExpr.get("op");

        return switch (operator) {
            case "+", "*", "/", "-", "%" -> intType();
            case "&&", "||", "==", "!=", "<", ">", "<=", ">=" -> boolType();
            default ->
                    throw new RuntimeException("Unknown operator '" + operator + "' of expression '" + binaryExpr + "'");
        };
    }

    private JmmType getUnaryExprType(JmmNode unaryPreExpr) {

        // Get operator
        String operator = unaryPreExpr.get("op");

        return switch (operator) {
            case "!" -> boolType();
            case "+", "-", "++", "--" -> intType();
            default ->
                    throw new RuntimeException("Unknown operator '" + operator + "' of expression '" + unaryPreExpr + "'");
        };
    }

    private JmmType getVarExprType(JmmNode varRefExpr) {
        var varName = varRefExpr.get("name");
        System.out.println("Looking up variable: " + varName);
        var maybeMethod = varRefExpr.getAncestor(METHOD_DECL);
        System.out.println("Method found: " + maybeMethod.isPresent());
        if (maybeMethod.isEmpty()) {
            return table.getField(varName)
                    .map(s -> s.type())
                    .orElseThrow(() -> new RuntimeException("Unknown variable: " + varName));
        }

        var sig = getMethodDeclSignature(maybeMethod.get());
        var method = table.getMethod(sig).orElseThrow();

        var asParam = method.getParameter(varName);
        if (asParam.isPresent()) return asParam.get().type();

        var asLocal = method.getLocalVariable(varName);
        if (asLocal.isPresent()) return asLocal.get().type();

        var asField = table.getField(varName);
        if (asField.isPresent()) return asField.get().type();

        throw new RuntimeException("Variable not found: " + varName);
    }

}
