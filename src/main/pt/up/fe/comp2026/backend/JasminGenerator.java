package pt.up.fe.comp2026.backend;

import org.specs.comp.ollir.*;
import org.specs.comp.ollir.inst.*;
import org.specs.comp.ollir.tree.TreeNode;
import org.specs.comp.ollir.type.ArrayType;
import org.specs.comp.ollir.type.BuiltinKind;
import org.specs.comp.ollir.type.BuiltinType;
import org.specs.comp.ollir.type.ClassType;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp2026.optimization.OptUtils;
import pt.up.fe.specs.util.SpecsCheck;
import pt.up.fe.specs.util.classmap.FunctionClassMap;
import pt.up.fe.specs.util.exceptions.NotImplementedException;
import pt.up.fe.specs.util.utilities.StringLines;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Generates Jasmin code from an OllirResult.
 * <p>
 * One JasminGenerator instance per OllirResult.
 */
public class JasminGenerator {

    private static final String NL = "\n";
    private static final String TAB = "   ";

    private final OllirResult ollirResult;

    private List<Report> reports;

    private String code;

    private Method currentMethod;

    boolean isInsideAssignment;

    private final JasminUtils types;
    private OptUtils utils;
    private final FunctionClassMap<TreeNode, String> generators;

    public JasminGenerator(OllirResult ollirResult) {
        this.ollirResult = ollirResult;

        reports = new ArrayList<>();
        code = null;
        currentMethod = null;
        isInsideAssignment = false;

        types = new JasminUtils(ollirResult);
        // Initialize everytime we start a method
        utils = null;
        this.generators = new FunctionClassMap<>();
        generators.put(ClassUnit.class, this::generateClassUnit);
        generators.put(Method.class, this::generateMethod);
        generators.put(AssignInstruction.class, this::generateAssign);
        generators.put(SingleOpInstruction.class, this::generateSingleOp);
        generators.put(LiteralElement.class, this::generateLiteral);
        generators.put(Operand.class, this::generateOperand);
        generators.put(BinaryOpInstruction.class, this::generateBinaryOp);
        generators.put(ReturnInstruction.class, this::generateReturn);
        generators.put(InvokeSpecialInstruction.class, this::generateInvokeSpecial);
        generators.put(NewInstruction.class, this::generateNew);
        generators.put(ArrayLengthInstruction.class, this::generateArrayLength);
    }


    private String apply(TreeNode node) {
        var code = new StringBuilder();

        // Print the corresponding OLLIR code as a comment
        //code.append("; ").append(node).append(NL);

        code.append(generators.apply(node));

        return code.toString();
    }

    public List<Report> getReports() {
        return reports;
    }

    public String build() {

        // This way, build is idempotent
        if (code == null) {
            code = apply(ollirResult.getOllirClass());
        }

        return code;
    }


    private String generateClassUnit(ClassUnit classUnit) {

        var code = new StringBuilder();

        // generate class name
        var nameWithPackage = ollirResult.getOllirClass().getClassFullyQualifiedName().replace('.', '/');
        code.append(".class public ").append(nameWithPackage).append(NL);

        // TODO: When you support 'extends', this must be updated
        var superClassName = ollirResult.getOllirClass().getSuperClass();
        var fullSuperClass = "java/lang/Object";  // default

        if (superClassName != null && !superClassName.isEmpty() && !superClassName.equals("Object")) {
            var imports = ollirResult.getOllirClass().getImports();
            String fullName = null;

            for (var importName : imports) {
                if (importName.endsWith("." + superClassName) || importName.equals(superClassName)) {
                    fullName = importName;
                    break;
                }
            }

            if (fullName == null) {
                fullName = "java.lang." + superClassName;
            }

            fullSuperClass = fullName.replace('.', '/');
        }

        code.append(".super ").append(fullSuperClass).append(NL);
        
        // Generate class fields
        for (var field : classUnit.getFields()) {
            var mod        = types.getModifier(field.getFieldAccessModifier());
            var staticMod  = field.isStaticField() ? "static " : "";
            var fieldName  = field.getFieldName();
            var descriptor = types.getTypeDescriptor(field.getFieldType());

            code.append(".field ");

            if (!mod.isEmpty()) {
                code.append(mod);
            }

            if (!staticMod.isEmpty()) {
                code.append(staticMod);
            }

            code.append('\'').append(fieldName).append('\'')
                    .append(" ")
                    .append(descriptor)
                    .append(NL);
        }
        for (var method : ollirResult.getOllirClass().getMethods())
            code.append(apply(method));


        return code.toString();
    }

    private int calculateLocals() {
        int locals = 0;

        for(var desc:currentMethod.getVarTable().values())
            if(locals < desc.getVirtualReg() + 1)
                locals = desc.getVirtualReg() + 1;
        return locals;
    }

    private String generateConstructor(Method method) {
        utils = new OptUtils(null);
        currentMethod = method;
        var code = new StringBuilder();
        code.append(".method ").append(types.getModifier(currentMethod.getMethodAccessModifier()))
                .append(method.getMethodName()).append("()")
                .append(types.getTypeDescriptor(method.getReturnType())).append(NL)
                .append(TAB).append(".limit locals ").append(calculateLocals()).append(NL)
                .append(TAB).append(".limit stack 1\n"); //Temp only

        for (var inst : method.getInstructions()) {
            var instCode = StringLines.getLines(apply(inst)).stream()
                    .collect(Collectors.joining(NL + TAB, TAB, NL));
            code.append(instCode);
        }
        code.append(TAB).append("return\n").append(".end method\n");
        return code.toString();
    }

    private String generateMethod(Method method) {
        //System.out.println("STARTING METHOD " + method.getMethodName());
        // set method

        if(method.isConstructMethod()) {
            return generateConstructor(method);
        }

        currentMethod = method;
        int locals = calculateLocals();

        // Initialize utils, to have fresh labels
        utils = new OptUtils(null);

        var code = new StringBuilder();

        // TODO: Modifier is hard-coded
        var modifier = types.getModifier(currentMethod.getMethodAccessModifier());


        var staticMod = method.isStaticMethod() ? "static " : "";

        var methodName = method.getMethodName();

        var params = method.getParams().stream()
                .map(p -> types.getTypeDescriptor(p.getType()))
                .collect(Collectors.joining());

        var returnType = types.getTypeDescriptor(method.getReturnType());

        code.append("\n.method ").append(modifier)
                .append(staticMod)
                .append(methodName).append("(").append(params).append(")").append(returnType).append(NL);


        var bodyCode = new StringBuilder();
        bodyCode.append(".limit locals ").append(locals).append(NL);
        for (var inst : method.getInstructions()) {
            var instCode = StringLines.getLines(apply(inst)).stream()
                    .collect(Collectors.joining(NL + TAB, TAB, NL));

            bodyCode.append(instCode);
        }

        // Add limits
        code.append(TAB).append(".limit stack 99").append(NL);
        //code.append(TAB).append(".limit locals 99").append(NL);

        code.append(TAB).append(bodyCode);

        code.append(".end method\n");
        //System.out.println("METHOD:\n" + code);
        // unset method
        currentMethod = null;
        //System.out.println("ENDING METHOD " + method.getMethodName());
        return code.toString();
    }

    private String generateAssign(AssignInstruction assign) {
        try {
            isInsideAssignment = true;
            var code = new StringBuilder();

            var lhs = assign.getDest();
            var rhs = assign.getRhs();

            if (rhs instanceof BinaryOpInstruction binOp) {
                var iincCode = tryGenerateIinc(lhs, binOp);
                if (iincCode != null) {
                    return iincCode;
                }
            }

            code.append(apply(assign.getRhs()));
            var operand = (Operand) lhs;
            var reg = currentMethod.getVarTable().get(operand.getName());
            code.append(types.getStore(reg)).append(NL);

            return code.toString();
        } finally {
            isInsideAssignment = false;
        }
    }
    private String tryGenerateIinc(Element dest, BinaryOpInstruction binOp) {
        if (binOp.getOperation().getOpType() != OperationType.ADD) {
            return null;
        }

        var left = binOp.getLeftOperand();
        var right = binOp.getRightOperand();

        if (isSameOperand(dest, left) && right instanceof LiteralElement lit) {
            int increment = Integer.parseInt(lit.getLiteral());
            int reg = currentMethod.getVarTable().get(((Operand)dest).getName()).getVirtualReg();
            return "iinc " + reg + " " + increment + NL;
        }

        if (left instanceof LiteralElement lit && isSameOperand(dest, right)) {
            int increment = Integer.parseInt(lit.getLiteral());
            int reg = currentMethod.getVarTable().get(((Operand)dest).getName()).getVirtualReg();
            return "iinc " + reg + " " + increment + NL;
        }

        return null;
    }

    private boolean isSameOperand(Element e1, Element e2) {
        if (!(e1 instanceof Operand op1) || !(e2 instanceof Operand op2)) {
            return false;
        }
        return op1.getName().equals(op2.getName());
    }

    private String generateSingleOp(SingleOpInstruction singleOp) {
        return apply(singleOp.getSingleOperand());
    }

    private String generateLiteral(LiteralElement literal) {
        var code = new StringBuilder();

        if (literal.getType() instanceof BuiltinType bt) {
            if (bt.getKind() == BuiltinKind.INT32) {
                int value = Integer.parseInt(literal.getLiteral());
                code.append(types.getConstInstruction(value)).append(NL);
            } else if (bt.getKind() == BuiltinKind.BOOLEAN) {
                int value = literal.getLiteral().equals("true") ? 1 : 0;
                code.append(types.getConstInstruction(value)).append(NL);
            } else {
                code.append("ldc ").append(literal.getLiteral()).append(NL);
            }
        } else {
            code.append("ldc ").append(literal.getLiteral()).append(NL);
        }

        return code.toString();
    }

    private String generateOperand(Operand operand) {
        // get register
        var reg = currentMethod.getVarTable().get(operand.getName());

        return types.getLoad(reg) + NL;
    }


    private String generateBinaryOp(BinaryOpInstruction binaryOp) {

        var code = new StringBuilder();

        // load values on the left and on the right
        code.append(apply(binaryOp.getLeftOperand()));
        code.append(apply(binaryOp.getRightOperand()));


        var typePrefix = types.getTypePrefix(binaryOp.getOperation().getTypeInfo());

        // apply operation
        var op = switch (binaryOp.getOperation().getOpType()) {
            case ADD -> "add";
            case SUB -> "sub";
            case MUL -> "mul";
            case DIV -> "div";
            default -> throw new NotImplementedException(binaryOp.getOperation().getOpType());
        };

        code.append(typePrefix + op).append(NL);

        return code.toString();
    }

    private String generateReturn(ReturnInstruction returnInst) {
        var code = new StringBuilder();

        var returnType = returnInst.getReturnType();

        var typePrefix = types.getTypePrefix(returnType);

        // Load operand into the stack, if present
        returnInst.getOperand().ifPresent(op -> code.append(apply(op)));

        code.append(typePrefix).append("return").append(NL);

        return code.toString();
    }

    private String generateInvokeSpecial(InvokeSpecialInstruction invokeInst) {
        var code = new StringBuilder();

        var target = invokeInst.getOperands().getFirst();
        code.append(apply(target));

        var params = new StringBuilder();
        for (int i = 1; i < invokeInst.getChildren().size() - 1; i++) { //The last one is the constructor name, I believe
            var arg = invokeInst.getOperands().get(i);
            code.append(apply(arg));
            params.append(types.getTypeDescriptor(arg.getType()));
        }

        var className = getFullClassName(((ClassType) target.getType()).getName());
        var method = ((LiteralElement) invokeInst.getChildren().getLast()).getLiteral();

        // 5. Build the invokespecial instruction with the correct parameters
        code.append("invokespecial ").append(className.replace('.', '/'))
                .append("/").append(method).append("(").append(params).append(")V\n");

        return code.toString();
    }

    private String generateNew(NewInstruction newInstruction) {
        var operand = newInstruction.getOperands().getFirst();
        var code = new StringBuilder();
        if(operand.getType() instanceof ClassType) {
            var name = ((ClassType) operand.getType()).getName();
            code.append("new ").append(getFullClassName(name)).append(NL);
        }

        return code.toString();
    }

    private String getFullClassName(String name) {
        var imports = ollirResult.getOllirClass().getImports();

        if(name.contains("java.lang.") || name.contains("java/lang/"))
            return name.replace('.', '/');

        if(name.equals("this") || name.equals(ollirResult.getOllirClass().getClassName()))
            return ollirResult.getOllirClass().getClassFullyQualifiedName().replace('.', '/');

        for (var importName : imports)
            if (importName.endsWith("." + name) || importName.equals(name))
                return importName.replace('.', '/');

        return "java/lang/" + name;
    }

    private String generateArrayLength(ArrayLengthInstruction arrayLengthInst) {
        var target = arrayLengthInst.getChildren().getFirst();
        return apply(target) +
                "arraylength\n";
    }
}