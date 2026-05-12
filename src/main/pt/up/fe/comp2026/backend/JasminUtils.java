package pt.up.fe.comp2026.backend;

import org.specs.comp.ollir.*;
import org.specs.comp.ollir.type.*;
import pt.up.fe.comp.jmm.analysis.table.reflection.Importer;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.specs.util.SpecsCheck;
import pt.up.fe.specs.util.exceptions.NotImplementedException;

import java.util.HashMap;
import java.util.Map;

public class JasminUtils {

    private final OllirResult ollirResult;

    private final Map<String, String> fullClassnames;

    private final Importer importer;

    public JasminUtils(OllirResult ollirResult) {
        this.ollirResult = ollirResult;
        this.importer = Importer.fromThisClassPath();
        // Build imports table
        fullClassnames = new HashMap<>();

        // Predefined classnames
        fullClassnames.put("this", ollirResult.getOllirClass().getClassName());
        // This will be get caught as STRING OLLIR element type.
        // And classes cannot be named String, since it is an OLLIR reserved keyword
        //imports.put("String", "java/lang/String");

        for (var fullImport : ollirResult.getOllirClass().getImports()) {
            var splitted = fullImport.split("\\.");

            // Last element will be the key
            var key = splitted[splitted.length - 1];
            fullClassnames.put(key, fullImport.replace('.', '/'));
        }
    }



    // Type prefix expansion (expanding int);    
    public String getTypePrefix(Type type) {

        if (type istanceof BuiltinType builtintype) {
            return switch (builtintype.getKind()) {
                case INT32, BOOLEAN -> "I";
                case VOID -> "";
                default -> "A";
            };
        }
        if (type instanceof ArrayType arrayType) return "A";
        return "A";
        }

        //System.out.println("[TODO] JasminUtils.getTypePrefix(): Assumes it is always int, needs to be expanded");
        //return "i"; - Code before type expansion
    }

    // Type descriptor expansion 
    public String getTypeDescriptor(Type type) {
        if (type instanceof BuiltinType builtinType) {
            return switch (builtinType.getKind()) {
                case INT32 -> "I";
                case BOOLEAN -> "Z";
                case VOID -> "V";
                case STRING -> "Ljava/lang/String;";
                default ->
                        throw new RuntimeException("Not implemented for element type '" + builtinType.getKind() + "'");
            };
        }
        if (type instanceof ArrayType arrayType) {
            return "[" + getTypeDescriptor(arrayType.getElementType());
        }
        if (type instanceof ClassType classType) {
            var name = ct.getName();
            var full = fullClassnames.get(name);
            return "L" +full.replace('.', '/') + ";";   
        }

        throw new RuntimeException("Not implemented for element type '" + type + "'");
    }




    public String getModifier(AccessModifier accessModifier) {
        return accessModifier.name().toLowerCase() + " ";
    }

    public String getLoad(Descriptor reg) {
        var prefix = getTypePrefix(reg.getVarType());
        var value = reg.getVirtualReg();

        return prefix + "load " + value;
    }

    public String getStore(Descriptor reg) {
        var prefix = getTypePrefix(reg.getVarType());
        var value = reg.getVirtualReg();

        return prefix + "store " + value;
    }



}
