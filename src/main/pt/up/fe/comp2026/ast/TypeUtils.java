package pt.up.fe.comp2026.ast;

import pt.up.fe.comp.jmm.analysis.table.MethodSymbol;
import pt.up.fe.comp.jmm.analysis.table.Signature;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.reflection.Importer;
import pt.up.fe.comp.jmm.analysis.table.type.impls.JmmPrimitiveType;
import pt.up.fe.comp.jmm.analysis.table.type.JmmType;
import pt.up.fe.comp.jmm.analysis.table.type.impls.JmmArrayType;
import pt.up.fe.comp.jmm.analysis.table.type.impls.JmmClassType;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2026.JavammParser;
import pt.up.fe.comp2026.jmm.ast.JmmKind;
import pt.up.fe.comp2026.symboltable.JmmSymbolTable;
import pt.up.fe.specs.util.SpecsCheck;
import pt.up.fe.specs.util.exceptions.NotImplementedException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static pt.up.fe.comp2026.jmm.ast.JmmKind.*;

//TODO: A LOT OF THIS THROWS HAVE TO BE CONVERTED TO REPORTS, IF POSSIBLE
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
        if(JmmKind.check(typeNode, BASE_TYPE)) {
            var name = typeNode.get("name");
            return JmmPrimitiveType.fromString(name).orElseThrow();
        }
        else if(JmmKind.check(typeNode, ARRAY_TYPE)) {
            int dim = 0;
            while (!JmmKind.check(typeNode, BASE_TYPE) && !JmmKind.check(typeNode, CLASS_TYPE) ) {
                typeNode = typeNode.getChild(0);
                dim++;
            }
            return new JmmArrayType(convertType(typeNode), dim);
        }
        return classType(typeNode);
    }

    /**
     * Gets the {@link JmmType} of an arbitrary expression.
     *
     * @param expr
     * @return
     */

    public JmmType getExprType(JmmNode expr) {
        return switch (expr.getKind()) {
            //Missing static references to classes
            case INTEGER_LITERAL, LENGTH_OPERATOR -> intType();
            case BINARY_EXPR -> getBinExprType(expr);
            case UNARY_EXPR -> getUnaryExprType(expr);
            case VAR_REF_EXPR -> getVarExprType(expr);
            case BOOLEAN_LITERAL -> boolType();
            case PARENTHESIS -> getExprType(expr.getChild(0));
            case THIS -> new JmmClassType(table.getFullyQualifiedName(), false, false);
            case IMPLICIT_THIS_FUNCTION_CALL -> getMethodCallType(expr, new JmmClassType(table.getFullyQualifiedName(), false, false));
            case ARRAY_ACCESS -> arrayAccessType(expr);
            case FUNCTION_CALL -> getMethodCallTypeFC(expr, getExprType(expr.getChild(0)).asClass());
            case CONSTRUCTOR -> classConstructorType(expr);
            case FIELD_ACCESS -> getFieldType(expr);
            case ARRAY_ALLOC -> {
                var type = expr.get("name");
                JmmPrimitiveType ftype = null; //Is it supposed to support classes?
                switch (type) {
                    case "int" ->  ftype = intType();
                    case "boolean" ->  ftype = boolType();
                    case "void" ->  ftype = voidType();
                }
                yield new JmmArrayType(ftype, expr.getChildren(ARRAY_INDEX).size());
            }
            case ARRAY_ALLOC_INIT -> {
                var type = expr.get("name");
                JmmPrimitiveType ftype = null; //Is it supposed to support classes?
                switch (type) {
                    case "int" ->  ftype = intType();
                    case "boolean" ->  ftype = boolType();
                    case "void" ->  ftype = voidType();
                }
                yield new JmmArrayType(ftype, expr.getChildren(ARRAY_DIM).size());
            }
            default ->
                    throw new UnsupportedOperationException("Can't compute type for expression kind '" + expr.getKind() + "'");
        };
    }

    private JmmType getTypeParam(JmmNode param) {
        var type = param.getChild(0);
        if(JmmKind.check(type, BASE_TYPE))
            return switch (type.get("name")) {
                case "int" -> TypeUtils.intType();
                case "boolean" -> TypeUtils.boolType();
                default ->
                        throw new RuntimeException("Can't compute type for param '" + param.get("name") + "'");
            };
        // If it isn't a base type
        if(JmmKind.check(type, ARRAY_TYPE)) {
            int dim = 0;
            while (!JmmKind.check(type, BASE_TYPE) && !JmmKind.check(type, CLASS_TYPE) ) {
                type = type.getChild(0);
                dim++;
            }
            return new JmmArrayType(convertType(type), dim);
        }
        else
            return classType(type);

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

    public JmmType getMethodCallType(JmmNode methodCall, JmmClassType classType) {
        var methodName = methodCall.get("func");
        var params = methodCall.getChildren(EXPR).stream().map(p -> TypeUtils.with(table).getExprType(p)).toList();
        var sig = new Signature(methodName, params);
        return findMethod(table, methodCall, sig, methodName);
    }

    public JmmType getMethodCallTypeFC(JmmNode methodCall, JmmClassType classType) {    //I have to try to unify it with the one above
        var methodName = methodCall.get("func");
        var target = methodCall.getChild(0);
        var params = methodCall.getChildren(EXPR).subList(1, methodCall.getChildren(EXPR).toArray().length).stream().map(p -> TypeUtils.with(table).getExprType(p)).toList();
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

        if(curTable.isEmpty())
            throw  new RuntimeException("Can't find method '" + methodName + "'");


        var fTable = curTable.get();
        return findMethod(fTable, methodCall, sig, methodName);
    }

    public JmmType getMethodType(JmmNode methodDecl) {
        METHOD_DECL.check(methodDecl);
        var type = methodDecl.getChild(0);
        if(JmmKind.check(type, BASE_TYPE))
            return switch (type.get("name")) {
                case "int" -> TypeUtils.intType();
                case "boolean" -> TypeUtils.boolType();
                case "void" -> TypeUtils.voidType();
                default ->
                        throw new RuntimeException("Can't compute type for param '" + methodDecl.get("name") + "'");
            };
        // If it isn't a base type
        if(JmmKind.check(type, ARRAY_TYPE)) {
            return arrayType(methodDecl);
        }
        else {
            return classType(type);
        }
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
        //System.out.println("Looking up variable: " + varName);
        var maybeMethod = varRefExpr.getAncestor(METHOD_DECL);
        //System.out.println("Method found: " + maybeMethod.isPresent());
        if (maybeMethod.isEmpty()) {
            return getFieldType(varRefExpr);
        }

        var sig = getMethodDeclSignature(maybeMethod.get());
        var method = table.getMethod(sig).orElseThrow();

        var asParam = method.getParameter(varName);
        if (asParam.isPresent()) return asParam.get().type();

        var asLocal = method.getLocalVariable(varName);
        if (asLocal.isPresent()) return asLocal.get().type();

        if(Importer.fromThisClassPath().isImplicitImport(varName)) return new JmmClassType(varName, true, true);

        var staticRef = table.getClassName();
        if(varName.equals(staticRef))
            return new JmmClassType(table.getFullyQualifiedName(), false, true);

        var staticRef2 = table.getImportNames().stream().filter(imp -> imp.equals(varName)).findFirst();
        if(staticRef2.isPresent())
            return new JmmClassType(table.getImportedFullyQualifiedName(varName).orElseThrow(), true, true);

        return getFieldType(varRefExpr);
        // Expand to search in imported classes
    }

    private JmmType getFieldType(JmmNode field) {
        var fieldName = field.get("name");
        var asField = table.getField(fieldName);
        if (asField.isPresent()) return asField.get().type();

        SymbolTable classTable = table;
        while(true) {
            if(classTable.getField(fieldName).isPresent())
                return classTable.getField(fieldName).get().type();
            var superClass = classTable.getSuperFullyQualifiedName();
            if(superClass == null)
                break;
            classTable = Importer.fromThisClassPath().getSymbolTableOf(superClass).orElseThrow();
        }

        throw new RuntimeException("Can't compute type for field '" + fieldName + "'");
    }

    public boolean isAssignable(JmmType destinationType, JmmType sourceType) {
        //Check base and array types
        if(destinationType.isPrimitive() || destinationType.isArray())
            if(destinationType.equals(sourceType))
                return true;

        var curType = new JmmClassType(table.getFullyQualifiedName(), false, false);

        if(destinationType.equals(curType) || sourceType.equals(curType)) {
            var destFQN = destinationType.asClass().fullyQualifiedName();
            var sourceFQN = sourceType.asClass().fullyQualifiedName();
            if(sourceFQN.equals(destFQN))
                return true;
            sourceFQN = table.getSuperFullyQualifiedName();
            while(sourceFQN != null) {
                if(sourceFQN.equals(destFQN))
                    return true;
                sourceFQN = Importer.fromThisClassPath().getSymbolTableOf(sourceFQN).orElseThrow().getSuperFullyQualifiedName();
            }
        }

        Class<?> destClass;
        Class<?> sourceClass;
        var checkClass = Importer.fromThisClassPath().tryClassOf(destinationType.asClass().fullyQualifiedName());
        if (checkClass.isPresent())
            destClass = checkClass.get();
        else {
            checkClass = Importer.fromThisClassPath().loadImplicit(destinationType.asClass().fullyQualifiedName());
            if (checkClass.isPresent())
                destClass = checkClass.get();
            else
                throw new RuntimeException("Can't compute type for type '" + destinationType.asClass().fullyQualifiedName() + "'");

        }

        checkClass = Importer.fromThisClassPath().tryClassOf(sourceType.asClass().fullyQualifiedName());
        if (checkClass.isPresent())
            sourceClass = checkClass.get();
        else {
            checkClass = Importer.fromThisClassPath().loadImplicit(sourceType.asClass().fullyQualifiedName());
            if (checkClass.isPresent())
                sourceClass = checkClass.get();
            else
                throw new RuntimeException("Can't compute type for type '" + sourceType.asClass().fullyQualifiedName() + "'");
        }
        return destClass.isAssignableFrom(sourceClass);
    }

    private JmmType arrayAccessType(JmmNode arrayAccessExpr) {
        var arrayID = arrayAccessExpr.getChild(0); //Basically, the array identifier/call, whatever.
        var arrayType = getExprType(arrayID);
        if(!arrayType.isArray())
            throw new RuntimeException("Can't compute type for array access expression '" + arrayAccessExpr + "'");
        var arrayDepth = arrayType.asArray().dimension();
        var accessDepth = arrayAccessExpr.getChildren(ARRAY_INDEX).size();
        if(arrayDepth > accessDepth)
            return new JmmArrayType(arrayType.asArray().itemType(), arrayDepth - accessDepth);
        else if(arrayDepth == accessDepth)
            return arrayType.asArray().itemType();
        throw new RuntimeException("Invalid access to array '" + arrayAccessExpr + "'");
    }

    private JmmClassType classType(JmmNode node) {
        String fqn;
        boolean imported = false;
        String name = node.get("name");
        if(name.equals(table.getClassName()))
            fqn = table.getFullyQualifiedName();
        else if(table.getImportNames().contains(name)) {
            imported = true;
            fqn = table.getImportedFullyQualifiedName(name).get();
        }
        else if(Importer.fromThisClassPath().isImplicitImport(name)) {
            imported = true;
            fqn = name;
        }
        else
            throw new RuntimeException("Can't find class with name '" + name + "'");
        return new JmmClassType(fqn, imported, false);  //staticRef not determined
    }

    private JmmArrayType arrayType(JmmNode node) {
        var type = node.getChild(0);
        int dim = 0;
        while (!JmmKind.check(type, BASE_TYPE) && !JmmKind.check(type, CLASS_TYPE) ) {
            type = type.getChild(0);
            dim++;
        }
        return new JmmArrayType(convertType(type), dim);
    }

    private JmmClassType classConstructorType(JmmNode node) {
        String fqn;
        boolean imported = false;
        String name = node.get("classType");
        if(name.equals(table.getClassName()))
            fqn = table.getFullyQualifiedName();
        else if(table.getImportNames().contains(name)) {
            imported = true;
            fqn = table.getImportedFullyQualifiedName(name).get();
        }
        else if(Importer.fromThisClassPath().isImplicitImport(name)) {
            imported = true;
            fqn = name;
        }
        else
            throw new RuntimeException("Can't find class with name '" + name + "'");

        var params = node.getChildren(EXPR);
        if(!params.isEmpty()) {
            var paramTypes = params.stream().map(this::getExprType).toList();
            var sig = new Signature(name, paramTypes);
            var st = Importer.fromThisClassPath().getSymbolTableOf(fqn).orElseThrow();
            return st.getMethod(sig).orElseThrow().returnType().asClass();  //A constructor with arguments has to be declared, right?
        }

        return new JmmClassType(fqn, imported, false);
    }

    private JmmType findMethod(SymbolTable table, JmmNode call, Signature sig, String methodName) {
        if (table.getMethod(sig).isEmpty() && table.getSuper() == null)
            throw new RuntimeException("Can't find method '" + methodName + "'");
        else if(table.getMethod(sig).isEmpty()) {
            var fqn = table.getSuperFullyQualifiedName();
            while(fqn != null) {
                var newTable = Importer.fromThisClassPath().getSymbolTableOf(fqn);
                if(newTable.isEmpty())
                    newTable = Importer.fromThisClassPath().tryImplicitImport(fqn);
                if(newTable.isEmpty())
                    break;

                else if(newTable.get().getMethod(sig).isPresent())
                    return newTable.get().getMethod(sig).get().returnType();
                fqn = newTable.get().getSuperFullyQualifiedName();
            }
            throw  new RuntimeException("Can't find method '" + methodName + "'");
        }
        return table.getMethod(sig).get().returnType();
    }
}
