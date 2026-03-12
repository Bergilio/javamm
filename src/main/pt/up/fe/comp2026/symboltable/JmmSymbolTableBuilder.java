package pt.up.fe.comp2026.symboltable;

import pt.up.fe.comp.jmm.analysis.table.MethodSymbol;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Visibility;
import pt.up.fe.comp.jmm.analysis.table.reflection.Importer;
import pt.up.fe.comp.jmm.analysis.table.type.impls.JmmPrimitiveType;
import pt.up.fe.comp.jmm.analysis.table.type.JmmType;
import pt.up.fe.comp.jmm.analysis.table.type.impls.JmmArrayType;
import pt.up.fe.comp.jmm.analysis.table.type.impls.JmmClassType;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp.jmm.utils.Attributes;
import pt.up.fe.comp2026.ast.NodeUtils;
import pt.up.fe.comp2026.ast.TypeUtils;
import pt.up.fe.comp2026.jmm.ast.JmmAttributes;
import pt.up.fe.specs.util.SpecsCheck;

import java.lang.reflect.Type;
import java.util.*;

import static pt.up.fe.comp2026.jmm.ast.JmmKind.*;

public class JmmSymbolTableBuilder {

    private final JmmNode root;
    private final Importer importer;
    public String className;
    private final List<Report> reports;
    private final List<String> imports;
    private final Map<String, String> declaredClasses;

    /**
     * Only build() can create new instances, this ensures that each instance is used only once,
     * and we do not have to worry about "cleaning state".
     */
    private JmmSymbolTableBuilder(JmmNode root) {
        this.root = root;
        reports = new ArrayList<>();
        imports = new ArrayList<>();
        declaredClasses = new HashMap<>();
        this.importer = Importer.fromThisClassPath();
    }

    private static Report newError(JmmNode node, String message) {
        return Report.newError(
                Stage.SEMANTIC,
                NodeUtils.getLine(node),
                NodeUtils.getColumn(node),
                message,
                null);
    }

    public static SymbolTableBuilderResult build(JmmNode root) {
        return new JmmSymbolTableBuilder(root).buildInternal();
    }

    private SymbolTableBuilderResult buildInternal() {


        var packageDecl = root.getChildren(PACKAGE_DECL).getFirst();
        var packagePathList = packageDecl.getObjectAsList("path", String.class);
        var packagePath = String.join(".", packagePathList);

        var classDecl = root.getObject("classNode", JmmNode.class);
        SpecsCheck.checkArgument(CLASS_DECL.check(classDecl), () -> "Expected a class declaration: " + classDecl);

        this.className = classDecl.get("name");
        var fullyQualifiedName = packagePath + "." + className;

        // Check if className is available
        if (declaredClasses.containsKey(className)) {
            reports.add(newError(root, "'" + className + "' is already defined in this compilation unit"));
        }
        declaredClasses.put(className, fullyQualifiedName);


        var methods = buildMethods(classDecl);

        var symbolTable = new JmmSymbolTable(imports, fullyQualifiedName, null, Collections.emptyList(), methods, importer);

        return new SymbolTableBuilderResult(symbolTable, reports);
    }

    private List<MethodSymbol> buildMethods(JmmNode classDecl) {

        return classDecl.getChildren(METHOD_DECL).stream()
                .map(this::buildMethod)
                .toList();

    }

    private MethodSymbol buildMethod(JmmNode method) {
        var methodName = method.get("name");

        //System.out.println("[TODO] JmmSymbolTableBuilder.buildMethod(): Assuming return type of method is always int, and always has a single int parameter, needs to be expanded");
        var type = method.getChild(0).get("name");
        JmmPrimitiveType returnType;
        switch (type) {
            case "int" -> returnType = TypeUtils.intType();
            case "bool" -> returnType = TypeUtils.boolType();
            case "void" -> returnType = TypeUtils.voidType();
            default ->
                    throw new RuntimeException("Can't compute type for method '" + method.get("name") + "'");
        };

        var children = method.getChildren(PARAM);
        List<Symbol> params = new ArrayList<>();

        for(JmmNode child : children) {
            params.add(new Symbol(getParamOrVarType(child), child.get(JmmAttributes.PARAM.NAME)));
        }

        //var params = List.of(new Symbol(TypeUtils.intType(), method.getChildren(PARAM).getFirst().get(JmmAttributes.PARAM.NAME)));

        //System.out.println("[TODO] JmmSymbolTableBuilder.buildMethod(): Assuming all VarDecls are ints, needs to be expanded");
        var locals = method.getChildren(VAR_DECL).stream()
                .map(varDecl -> new Symbol(getParamOrVarType(varDecl), varDecl.get(JmmAttributes.VAR_DECL.NAME)))
                .toList();

        var visibility =  getVisibility(method);
        var isStatic = method.getBoolean(JmmAttributes.METHOD_DECL.IS_STATIC, false);
        return new MethodSymbol(methodName, returnType, params, locals, isStatic, visibility);
    }

    // Missing array Param
    private JmmType getParamOrVarType(JmmNode node) {
        return switch (node.getChild(0).get("name")) {
            case "int" -> TypeUtils.intType();
            case "bool" -> TypeUtils.boolType();
            default ->
                    throw new RuntimeException("Can't compute type for variable/param '" + node.get("name") + "'");
        };
    }

    private Visibility getVisibility(JmmNode node) {
        var visibility = node.getOptional("visibility");
        if(visibility.isEmpty() || visibility.equals("public"))
            return Visibility.PUBLIC;
        else if (visibility.equals("private"))
            return Visibility.PRIVATE;
        else
            return Visibility.PROTECTED;
    }
}
