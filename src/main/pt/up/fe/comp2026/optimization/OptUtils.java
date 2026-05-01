package pt.up.fe.comp2026.optimization;

import org.specs.comp.ollir.Element;
import org.specs.comp.ollir.LiteralElement;
import pt.up.fe.comp.jmm.analysis.table.type.impls.JmmPrimitiveType;
import pt.up.fe.comp.jmm.analysis.table.type.JmmType;
import pt.up.fe.comp.jmm.analysis.table.type.impls.JmmArrayType;
import pt.up.fe.comp.jmm.analysis.table.type.impls.JmmClassType;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2026.ast.TypeUtils;
import pt.up.fe.specs.util.collections.AccumulatorMap;

import java.util.Set;

import static pt.up.fe.comp2026.jmm.ast.JmmKind.TYPE;

/**
 * Utility methods related to the optimization middle-end.
 */
public class OptUtils {

    private static final Set<String> OLLIR_KEYWORDS = Set.of("bool", "i32", "V", "array", "String", "final", "goto", "if", "import", "interface", "new", "package"
            , "private", "protected", "public", "ret", "static", "this", "ldc", "invokespecial", "invokevirtual", "invokestatic", "arraylength", "getfield", "putfield", "getstatic", "putstatic",
            ".method", ".construct", ".field", "extends"
    );


    private final AccumulatorMap<String> temporaries;

    private final TypeUtils types;

    public OptUtils(TypeUtils types) {
        this.types = types;
        this.temporaries = new AccumulatorMap<>();
    }


    public String nextTemp() {
        return nextTemp("tmp");
    }


    public String nextTemp(String prefix) {

        // Subtract 1 because the base is 1
        var nextTempNum = temporaries.add(prefix) - 1;

        return prefix + nextTempNum;
    }


    public String toOllirType(JmmNode typeNode) {

        TYPE.checkOrThrow(typeNode);

        return toOllirType(types.convertType(typeNode));
    }

    public String toOllirType(JmmType type) {
        if (type instanceof JmmPrimitiveType primitive) {
            return switch (primitive) {
                case INT -> ".i32";
                case BOOLEAN -> ".bool";
                case VOID -> ".V";
                default -> throw new RuntimeException("Unknown primitive type: " + primitive.name());
            };
        }

        if (type instanceof JmmArrayType(JmmType itemType, int dimension)) {
            // Recursively build the array type suffix
            return ".array".repeat(dimension) + toOllirType(itemType);
        }

        if (type instanceof JmmClassType classType) {
            // Use the simple class name (last part of the fully qualified name)
            var fqn = classType.fullyQualifiedName();
            var simpleName = fqn.contains(".") ? fqn.substring(fqn.lastIndexOf('.') + 1) : fqn;
            return "." + simpleName;
        }

        throw new RuntimeException("Unknown type kind: " + type);
    }

    public String sanitizeId(String id) {
        // If id is an OLLIR keyword, return a string literal with the id
        if (OLLIR_KEYWORDS.contains(id)) {
            return '"' + id + '"';
        }

        // If id starts with $, escape it
        if (id.startsWith("$")) {
            return '"' + id + '"';
        }

        return id;
    }


}