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
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp.jmm.utils.Attributes;
import pt.up.fe.comp2026.ast.NodeUtils;
import pt.up.fe.comp2026.ast.TypeUtils;
import pt.up.fe.comp2026.jmm.ast.JmmAttributes;
import pt.up.fe.comp2026.jmm.ast.JmmKind;
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

        imports.addAll(getImports());

        var classDecl = root.getObject("classNode", JmmNode.class);
        SpecsCheck.checkArgument(CLASS_DECL.check(classDecl), () -> "Expected a class declaration: " + classDecl);

        this.className = classDecl.get("name");
        var fullyQualifiedName = packagePath + "." + className;

        // Check if className is available
        if (declaredClasses.containsKey(className)) {
            reports.add(newError(root, "'" + className + "' is already defined in this compilation unit"));
        }
        declaredClasses.put(className, fullyQualifiedName);

        // TODO: Implement support for the "extends"
        var extension = classDecl.getOptional("extension");
        if(extension.isPresent() && extension.get().equals(className)) {
            reports.add(newError(classDecl, "Class '" + className + "' cannot extend itself"));
        }

        String superQualifiedNameName;
        if(extension.isPresent()) {
            var name = extension.get();
            superQualifiedNameName = imports.stream()
                    .filter(imp -> imp.endsWith("." + name) || imp.equals(name))
                    .findFirst()
                    .orElse(null);  // Same package, I think

            // Not sure if this is adequate
            if(superQualifiedNameName == null && importer.isImplicitImport(name))
                superQualifiedNameName = name;

            else if(superQualifiedNameName == null && importer.inClassPath(name))
                superQualifiedNameName = packagePath + "." + name;

            else if(superQualifiedNameName == null) {
                reports.add(newError(classDecl, "Class '" + name + "' is not imported"));
                superQualifiedNameName = name;
            }
        }
        else
            superQualifiedNameName = "Object";

        var methods = buildMethods(classDecl);
        var fields = buildVars(classDecl);

        var symbolTable = new JmmSymbolTable(imports, fullyQualifiedName, superQualifiedNameName, fields, methods, importer);

        return new SymbolTableBuilderResult(symbolTable, reports);
    }

    private List<String> getImports() {
        Map<String, String> name_fqn = new HashMap<>();
        for(var imp : this.root.getChildren(IMPORT_DECL)) {
            var base_name = imp.getObjectAsList("path", String.class).getLast();
            var fqn = fullyQualifiedNameImport(imp);
            if(name_fqn.containsKey(base_name) && !name_fqn.get(base_name).equals(fqn))
                reports.add(newError(imp, "Imports with the same base name, but not fqn"));
            else if(!name_fqn.containsKey(base_name))
                name_fqn.put(base_name, fqn);
        }

        return name_fqn.values().stream().toList();
    }

    private String fullyQualifiedNameImport(JmmNode imp) {
        return String.join(".", imp.getObjectAsList("path", String.class));
    }

    private List<Symbol> buildVars(JmmNode node) {
        var vars = node.getChildren(VAR_DECL);
        var result = new ArrayList<Symbol>();
        var names = new ArrayList<String>();
        for(var v : vars) {
            var symbol = buildVar(v);
            if(symbol == null)
                continue;
            if(!names.contains(symbol.name())) {
                result.add(symbol);
                names.add(symbol.name());
            }
            else
                reports.add(newError(node, "Duplicate variable definition: " + symbol.name()));
        }
        return result;
    }

    private Symbol buildVar(JmmNode varDecl) {
        var type = getType(varDecl.getChild(0));
        if(type == null)
            return null;
        return new Symbol(type, varDecl.get(JmmAttributes.VAR_DECL.NAME));
    }

    private List<MethodSymbol> buildMethods(JmmNode classDecl) {

        return classDecl.getChildren(METHOD_DECL).stream()
                .map(this::buildMethod)
                .toList();
    }

    private MethodSymbol buildMethod(JmmNode method) {
        var methodName = method.get("name");

        //System.out.println("[TODO] JmmSymbolTableBuilder.buildMethod(): Assuming return type of method is always int, and always has a single int parameter, needs to be expanded");
        JmmType returnType = getType(method.getChild(0));   //ReturnType in case you don't remember
        var children = method.getChildren(PARAM);
        List<Symbol> params = new ArrayList<>();
        var paramsNames = new ArrayList<String>();

        for(JmmNode child : children) {
            var symbol = new Symbol(getType(child.getChild(0)), child.get(JmmAttributes.PARAM.NAME));
            if(!paramsNames.contains(symbol.name())) {
                params.add(symbol);
                paramsNames.add(symbol.name());
            }
            else
                reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, method.getLine(), "Duplicate parameter name: " + symbol.name()));
        }

        //var params = List.of(new Symbol(TypeUtils.intType(), method.getChildren(PARAM).getFirst().get(JmmAttributes.PARAM.NAME)));

        //System.out.println("[TODO] JmmSymbolTableBuilder.buildMethod(): Assuming all VarDecls are ints, needs to be expanded");
        var locals = buildVars(method);

        for(var l : locals) {
            if(paramsNames.contains(l.name()))
                reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, method.getLine(), "Parameter and Local Variable sharing the same name: " + l.name()));
        }

        var visibility =  Visibility.fromString(method.getOptional("visibility").orElse(""));
        var isStatic = method.getBoolean(JmmAttributes.METHOD_DECL.IS_STATIC, false);
        return new MethodSymbol(methodName, returnType, params, locals, isStatic, visibility);
    }

    private JmmType getType(JmmNode node) {
        if(JmmKind.check(node, BASE_TYPE))
            return switch (node.get("name")) {
                case "int" -> TypeUtils.intType();
                case "boolean" -> TypeUtils.boolType();
                case "void" -> {
                    if (node.getParent().isInstance(PARAM) || node.getParent().isInstance(VAR_DECL)) // To allow the merge of VAR_DECL, PARAM and METHOD_DECL type extraction
                        reports.add(newError(node, "Param or Variable declaration can't be of type void."));
                    yield TypeUtils.voidType();
                }
                default -> {
                        reports.add(newError(node, "Can't compute type for variable/param '" + node.get("name") + "'"));
                        yield null;
                }
            };

        // If it isn't a base type
        if(JmmKind.check(node, ARRAY_TYPE)) {
            int dim = 0;
            while (!JmmKind.check(node, BASE_TYPE) && !JmmKind.check(node, CLASS_TYPE) ) {
                node = node.getChild(0);
                dim++;
            }

            return new JmmArrayType(getType(node), dim);
        }
        else {
            String fqn;
            boolean imported = false;
            String name = node.get("name");
            if(Objects.equals(name, className))
                fqn = declaredClasses.get(name);
            else if(imports.stream()    //I think this could be simplified if the imports are added to declaredClasses
                    .anyMatch(imp -> imp.endsWith("." + name) || imp.equals(name))) {
                imported = true;
                fqn = imports.stream()
                        .filter(imp -> imp.endsWith("." + name) || imp.equals(name))
                        .findFirst().get();
            }
            else if(importer.isImplicitImport(name)) {
                imported = true;
                fqn = name;
            }
            else {
                reports.add(newError(node, "Can't compute type of '" + name + "'"));
                return null;
            }
            return new JmmClassType(fqn, imported, false);  //staticRef not determined
        }
    }
}
