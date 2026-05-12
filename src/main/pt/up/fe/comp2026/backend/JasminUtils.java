package pt.up.fe.comp2026.backend;

import org.specs.comp.ollir.*;
import org.specs.comp.ollir.type.*;
import pt.up.fe.comp.jmm.analysis.table.reflection.Importer;
import pt.up.fe.comp.jmm.ollir.OllirResult;

import java.util.HashMap;
import java.util.Map;

public class JasminUtils {

    private final OllirResult ollirResult;
    private final Map<String, String> fullClassnames;
    private final Importer importer;

    public JasminUtils(OllirResult ollirResult) {
        this.ollirResult = ollirResult;
        this.importer = Importer.fromThisClassPath();

        fullClassnames = new HashMap<>();

        fullClassnames.put("this", ollirResult.getOllirClass().getClassName());

        for (var fullImport : ollirResult.getOllirClass().getImports()) {
            var splitted = fullImport.split("\\.");
            // Last element will be the key
            var key = splitted[splitted.length - 1];
            fullClassnames.put(key, fullImport.replace('.', '/'));
        }
    }

    public String getTypePrefix(Type type) {
        if (type instanceof BuiltinType builtinType) {
            return switch (builtinType.getKind()) {
                case INT32, BOOLEAN -> "i";
                case VOID -> "";
                default -> "a";
            };
        }
        if (type instanceof ArrayType) return "a";
        if (type instanceof ClassType) return "a";
        return "a";
    }

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
            var name = classType.getName();
            var full = fullClassnames.getOrDefault(name, name);
            return "L" + full.replace('.', '/') + ";";
        }

        throw new RuntimeException("Not implemented for element type '" + type + "'");
    }

    public String getModifier(AccessModifier accessModifier) {
        return accessModifier.name().toLowerCase() + " ";
    }

    public String getLoad(Descriptor reg) {
        var prefix = getTypePrefix(reg.getVarType());
        var value = reg.getVirtualReg();

        if (value >= 0 && value <= 3) {
            return prefix + "load_" + value;
        }

        return prefix + "load " + value;
    }

    public String getStore(Descriptor reg) {
        var prefix = getTypePrefix(reg.getVarType());
        var value = reg.getVirtualReg();

        if (value >= 0 && value <= 3) {
            return prefix + "store_" + value;
        }

        return prefix + "store " + value;
    }

    public String getConstInstruction(int value) {
        if (value == -1) {
            return "iconst_m1";
        }

        if (value >= 0 && value <= 5) {
            return "iconst_" + value;
        }

        if (value >= -128 && value <= 127) {
            return "bipush " + value;
        }

        if (value >= -32768 && value <= 32767) {
            return "sipush " + value;
        }

        return "ldc " + valu;
    }

    public String getMethodDescriptor(Method method) {
        StringBuilder descriptor = new StringBuilder("(");

        for (Element param : method.getParams()) {
            descriptor.append(getTypeDescriptor(param.getType()));
        }

        descriptor.append(")");

        descriptor.append(getTypeDescriptor(method.getReturnType()));

        return descriptor.toString();
    }

    public String getArrayLoadInstruction(Type elementType) {
        if (elementType instanceof BuiltinType builtinType) {
            return switch (builtinType.getKind()) {
                case INT32 -> "iaload";
                case BOOLEAN -> "baload";
                default -> "aaload";
            };
        }
        return "aaload";
    }

    public String getArrayStoreInstruction(Type elementType) {
        if (elementType instanceof BuiltinType builtinType) {
            return switch (builtinType.getKind()) {
                case INT32 -> "iastore";
                case BOOLEAN -> "bastore";
                default -> "aastore";
            };
        }
        return "aastore";
    }

    public String getArrayTypeCode(Type elementType) {
        if (elementType instanceof BuiltinType builtinType) {
            return switch (builtinType.getKind()) {
                case INT32 -> "int";
                case BOOLEAN -> "boolean";
                default -> throw new RuntimeException("Unsupported array element type: " + builtinType.getKind());
            };
        }
        throw new RuntimeException("newarray only supports primitive types");
    }

    public String getReturnInstruction(Type returnType) {
        if (returnType instanceof BuiltinType builtinType) {
            return switch (builtinType.getKind()) {
                case VOID -> "return";
                case INT32, BOOLEAN -> "ireturn";
                default -> "areturn";
            };
        }
        return "areturn";
    }

    public String getFullClassName(String className) {
        return fullClassnames.getOrDefault(className, className);
    }

    public boolean isPrimitive(Type type) {
        if (type instanceof BuiltinType builtinType) {
            return builtinType.getKind() == BuiltinKind.INT32 ||
                    builtinType.getKind() == BuiltinKind.BOOLEAN;
        }
        return false;
    }

    public OllirResult getOllirResult() {
        return ollirResult;
    }

    public Importer getImporter() {
        return importer;
    }
}

