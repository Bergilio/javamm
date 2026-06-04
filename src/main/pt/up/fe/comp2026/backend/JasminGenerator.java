package pt.up.fe.comp2026.backend;

import org.specs.comp.ollir.*;
import org.specs.comp.ollir.inst.*;
import org.specs.comp.ollir.tree.TreeNode;
import org.specs.comp.ollir.type.*;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp2026.optimization.OptUtils;
import pt.up.fe.specs.util.classmap.FunctionClassMap;
import pt.up.fe.specs.util.exceptions.NotImplementedException;
import pt.up.fe.specs.util.utilities.StringLines;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
    private StackWorker stackWorker;

    private int labelCounter;

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
        labelCounter = 0;

        types = new JasminUtils(ollirResult);
        // Initialize everytime we start a method
        utils = null;
        this.generators = new FunctionClassMap<>();
        generators.put(ClassUnit.class, this::generateClassUnit);
        generators.put(Method.class, this::generateMethod);
        generators.put(AssignInstruction.class, this::generateAssign);
        generators.put(SingleOpInstruction.class, this::generateSingleOp);
        generators.put(LiteralElement.class, this::generateLiteral);
        generators.put(ArrayOperand.class, this::generateArrayOperand);
        generators.put(Operand.class, this::generateOperand);
        generators.put(UnaryOpInstruction.class, this::generateUnaryOp);
        generators.put(BinaryOpInstruction.class, this::generateBinaryOp);
        generators.put(GotoInstruction.class, this::generateGoto);
        generators.put(SingleOpCondInstruction.class, this::generateSingleOpCond);
        generators.put(OpCondInstruction.class, this::generateOpCond);
        generators.put(ReturnInstruction.class, this::generateReturn);
        generators.put(InvokeVirtualInstruction.class, this::generateInvokeVirtual);
        generators.put(InvokeStaticInstruction.class, this::generateInvokeStatic);
        generators.put(InvokeSpecialInstruction.class, this::generateInvokeSpecial);
        generators.put(NewInstruction.class, this::generateNew);
        generators.put(ArrayLengthInstruction.class, this::generateArrayLength);
        generators.put(PutFieldInstruction.class, this::generatePutFieldInst);
        generators.put(GetFieldInstruction.class, this::generateGetFieldInst);
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
            else
                code.append("public ");

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
        code.append(".method ").append("public ")
                .append(method.getMethodName()).append("()")
                .append(types.getTypeDescriptor(method.getReturnType())).append(NL)
                .append(TAB).append(".limit locals ").append(calculateLocals()).append(NL)
                .append(TAB);

        var bodyCode = new StringBuilder();
        for (var inst : method.getInstructions()) {
            var instCode = StringLines.getLines(apply(inst)).stream()
                    .collect(Collectors.joining(NL + TAB, TAB, NL));
            bodyCode.append(instCode).append(handleStackLeak(inst));
        }
        code.append(".limit stack ").append(stackWorker.getMaxSize()).append(NL).append(bodyCode);
        code.append(TAB).append("return\n").append(".end method\n");
        return code.toString();
    }

    private String generateMethod(Method method) {
        // set method
        currentMethod = method;
        stackWorker = new StackWorker();
        if(method.isConstructMethod()) {
            return generateConstructor(method);
        }

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
            for (var label : method.getLabels(inst))
                bodyCode.append(label).append(":").append(NL);

            var instCode = StringLines.getLines(apply(inst)).stream()
                    .collect(Collectors.joining(NL + TAB, TAB, NL));

            bodyCode.append(instCode).append(handleStackLeak(inst));
        }

        // Add limits
        code.append(TAB).append(".limit stack ").append(stackWorker.getMaxSize()).append(NL);
        //code.append(TAB).append(".limit locals 99").append(NL);

        code.append(TAB).append(bodyCode);

        code.append(".end method\n");
        // unset method
        currentMethod = null;
        return code.toString();
    }

    //Used to check for useless results like useless returns or ops
    private String handleStackLeak(Instruction inst) {
        if (inst instanceof InvokeVirtualInstruction invoke) {
            var returnDesc = types.getTypeDescriptor(invoke.getReturnType());
            if (!returnDesc.equals("V")) {
                stackWorker.pop();
                return "pop" + NL;
            }
        }
        else if (inst instanceof InvokeStaticInstruction invoke) {
            var returnDesc = types.getTypeDescriptor(invoke.getReturnType());
            if (!returnDesc.equals("V")) {
                stackWorker.pop();
                return "pop" + NL;
            }
        }
        else if (inst instanceof SingleOpInstruction singleOp) {
            var type = singleOp.getSingleOperand().getType();
            if (!(type instanceof BuiltinType builtinType && builtinType.getKind() == BuiltinKind.VOID)) {
                stackWorker.pop();
                return "pop" + NL;
            }
        }
        return "";
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

            if(lhs instanceof ArrayOperand) {
                var arrayOp = (ArrayOperand) lhs;
                var reg = currentMethod.getVarTable().get(arrayOp.getName());
                var index = arrayOp.getIndexOperands().getFirst();
                code.append(types.getLoad(reg, stackWorker)).append(NL).append(apply(index)).append(apply(assign.getRhs()))
                        .append(types.getTypePrefix(arrayOp.getType())).append("astore").append(NL);
                stackWorker.pop();
                stackWorker.pop();
                stackWorker.pop();
            }
            else {
                code.append(apply(assign.getRhs()));
                var operand = (Operand) lhs;
                var reg = currentMethod.getVarTable().get(operand.getName());
                code.append(types.getStore(reg, stackWorker)).append(NL);
            }
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
            if (bt.getKind() == BuiltinKind.INT32 || bt.getKind() == BuiltinKind.BOOLEAN) {
                int value = Integer.parseInt(literal.getLiteral());
                code.append(types.getConstInstruction(value, stackWorker)).append(NL);
            }
            else {
                stackWorker.push();
                code.append("ldc ").append(literal.getLiteral()).append(NL);
            }
        } else {
            stackWorker.push();
            code.append("ldc ").append(literal.getLiteral()).append(NL);
        }

        return code.toString();
    }

    private String generateOperand(Operand operand) {
        // get register
        var reg = currentMethod.getVarTable().get(operand.getName());

        return types.getLoad(reg, stackWorker) + NL;
    }

    private String generateArrayOperand(ArrayOperand arrayOperand) {
        var index = arrayOperand.getIndexOperands().getFirst();
        var reg = currentMethod.getVarTable().get(arrayOperand.getName());
        var arrayType = arrayOperand.getType();
        var code = types.getLoad(reg, stackWorker) + NL + apply(index) +
                types.getTypePrefix(arrayType) + "aload" + NL;
        stackWorker.pop();
        stackWorker.pop();
        stackWorker.push();
        return code;
    }

    private String generateUnaryOp(UnaryOpInstruction unaryOp) {
        var code = new StringBuilder();
        code.append(apply(unaryOp.getOperand()));
        code.append("iconst_1").append(NL); //+1
        stackWorker.push();
        code.append("ixor").append(NL); //-2 + 1
        stackWorker.pop();
        stackWorker.pop();
        stackWorker.push();
        return code.toString();
    }

    private String generateBinaryOp(BinaryOpInstruction binaryOp) {
        var code = new StringBuilder();
        code.append(apply(binaryOp.getLeftOperand()));
        code.append(apply(binaryOp.getRightOperand()));

        var typePrefix = types.getTypePrefix(binaryOp.getOperation().getTypeInfo());
        var opType = binaryOp.getOperation().getOpType();

        switch (opType) {
            case ADD -> {
                stackWorker.pop();
                stackWorker.pop();
                stackWorker.push();
                code.append(typePrefix).append("add").append(NL);
            }
            case SUB -> {
                stackWorker.pop();
                stackWorker.pop();
                stackWorker.push();
                code.append(typePrefix).append("sub").append(NL);
            }
            case MUL -> {
                stackWorker.pop();
                stackWorker.pop();
                stackWorker.push();
                code.append(typePrefix).append("mul").append(NL);
            }
            case DIV -> {
                stackWorker.pop();
                stackWorker.pop();
                stackWorker.push();
                code.append(typePrefix).append("div").append(NL);
            }
            case REM -> {
                stackWorker.pop();
                stackWorker.pop();
                stackWorker.push();
                code.append(typePrefix).append("rem").append(NL);
            }
            case BITWISE_AND, LOGICAL_AND -> {
                stackWorker.pop();
                stackWorker.pop();
                stackWorker.push();
                code.append(typePrefix).append("and").append(NL);
            }
            case BITWISE_OR, LOGICAL_OR -> {
                stackWorker.pop();
                stackWorker.pop();
                stackWorker.push();
                code.append(typePrefix).append("or").append(NL);
            }
            case XOR -> {
                stackWorker.pop();
                stackWorker.pop();
                stackWorker.push();
                code.append(typePrefix).append("xor").append(NL);
            }
            case LTH, GTH, LTE, GTE, EQ, NEQ -> {
                var trueLabel = "cmp_true_" + (labelCounter++);
                var endLabel  = "cmp_end_"  + (labelCounter++);
                var branch = switch (opType) {
                    case LTH -> "if_icmplt";
                    case GTH -> "if_icmpgt";
                    case LTE -> "if_icmple";
                    case GTE -> "if_icmpge";
                    case EQ  -> "if_icmpeq";
                    case NEQ -> "if_icmpne";
                    default  -> throw new RuntimeException("unreachable");
                };
                stackWorker.pop();
                stackWorker.pop();
                stackWorker.push();
                code.append(branch).append(" ").append(trueLabel).append(NL)
                        .append("iconst_0").append(NL)
                        .append("goto ").append(endLabel).append(NL)
                        .append(trueLabel).append(":").append(NL)
                        .append("iconst_1").append(NL)
                        .append(endLabel).append(":").append(NL);
            }
            default -> throw new NotImplementedException(opType);
        }

        return code.toString();
    }

    private String generateGoto(GotoInstruction gotoInst) {
        return "goto " + gotoInst.getLabel() + NL;
    }

    private String generateSingleOpCond(SingleOpCondInstruction condInst) {
        var code = new StringBuilder();
        code.append(apply(condInst.getCondition().getSingleOperand()));
        code.append("ifne ").append(condInst.getLabel()).append(NL);
        stackWorker.pop();
        return code.toString();
    }

    private String generateOpCond(OpCondInstruction condInst) {
        var code = new StringBuilder();
        var cond = condInst.getCondition();
        if(cond instanceof BinaryOpInstruction binOp) {
            var leftOp = binOp.getLeftOperand();
            var rightOp = binOp.getRightOperand();
            var leftIsZ = leftOp.isLiteral() && ((LiteralElement) leftOp).getLiteral().equals("0");
            var rightIsZ = rightOp.isLiteral() && ((LiteralElement) rightOp).getLiteral().equals("0");

            String branchInstr;
            if(rightIsZ) {
                code.append(apply(binOp.getLeftOperand()));
                stackWorker.pop();
                branchInstr = switch (binOp.getOperation().getOpType()) {
                    case LTH -> "iflt";
                    case GTH -> "ifgt";
                    case LTE -> "ifle";
                    case GTE -> "ifge";
                    case EQ -> "ifeq";
                    case NEQ -> "ifne";
                    default -> throw new RuntimeException("Unknown condition op: " + binOp.getOperation().getOpType());
                };
            }
            else if (leftIsZ) {
                code.append(apply(binOp.getRightOperand()));
                stackWorker.pop();
                branchInstr = switch (binOp.getOperation().getOpType()) {
                    case LTH -> "ifgt";
                    case GTH -> "iflt";
                    case LTE -> "ifge";
                    case GTE -> "ifle";
                    case EQ -> "ifeq";
                    case NEQ -> "ifne";
                    default -> throw new RuntimeException("Unknown condition op: " + binOp.getOperation().getOpType());
                };
            }
            else {
                code.append(apply(binOp.getLeftOperand()));
                code.append(apply(binOp.getRightOperand()));
                stackWorker.pop();
                stackWorker.pop();
                branchInstr = switch (binOp.getOperation().getOpType()) {
                    case LTH -> "if_icmplt";
                    case GTH -> "if_icmpgt";
                    case LTE -> "if_icmple";
                    case GTE -> "if_icmpge";
                    case EQ -> "if_icmpeq";
                    case NEQ -> "if_icmpne";
                    default -> throw new RuntimeException("Unknown condition op: " + binOp.getOperation().getOpType());
                };
            }
            code.append(branchInstr).append(" ").append(condInst.getLabel()).append(NL);
        }
        else {
            var unOp = (UnaryOpInstruction) cond; //It should be a !, right?
            code.append(apply(unOp.getOperand()));
            stackWorker.pop();
            code.append("ifeq ").append(condInst.getLabel()).append(NL);
        }
        return code.toString();
    }

    private String generateInvokeVirtual(InvokeVirtualInstruction invokeInst) {
        var code = new StringBuilder();
        var caller = invokeInst.getCaller();
        code.append(apply(caller));

        var paramDesc = new StringBuilder();
        for (var arg : invokeInst.getArguments()) {
            code.append(apply(arg));
            paramDesc.append(types.getTypeDescriptor(arg.getType()));
        }

        var className = getFullClassName(((ClassType) caller.getType()).getName());
        var methodName = ((LiteralElement) invokeInst.getMethodName()).getLiteral();
        var returnDesc = types.getTypeDescriptor(invokeInst.getReturnType());

        code.append("invokevirtual ").append(className.replace('.', '/'))
                .append("/").append(methodName).append("(").append(paramDesc).append(")").append(returnDesc).append(NL);
        stackWorker.pop();
        for(int i = 0; i < invokeInst.getArguments().size(); i++)
            stackWorker.pop();
        if(!Objects.equals(returnDesc, "V"))
            stackWorker.push();

        return code.toString();
    }

    private String generateInvokeStatic(InvokeStaticInstruction invokeInst) {
        var code = new StringBuilder();

        var paramDesc = new StringBuilder();
        for (var arg : invokeInst.getArguments()) {
            code.append(apply(arg));
            paramDesc.append(types.getTypeDescriptor(arg.getType()));
        }

        var callerType = invokeInst.getCaller().getType();
        String className;
        if (callerType instanceof ClassType ct)
            className = getFullClassName(ct.getName());
        else
            className = getFullClassName(((Operand) invokeInst.getCaller()).getName());

        var methodName = ((LiteralElement) invokeInst.getMethodName()).getLiteral();
        var returnDesc = types.getTypeDescriptor(invokeInst.getReturnType());

        code.append("invokestatic ").append(className.replace('.', '/'))
                .append("/").append(methodName)
                .append("(").append(paramDesc).append(")").append(returnDesc).append(NL);
        for(int i = 0; i < invokeInst.getArguments().size(); i++)
            stackWorker.pop();
        if(!Objects.equals(returnDesc, "V"))
            stackWorker.push();

        return code.toString();
    }

    private String generateReturn(ReturnInstruction returnInst) {
        var code = new StringBuilder();

        var returnType = returnInst.getReturnType();

        var typePrefix = types.getTypePrefix(returnType);

        // Load operand into the stack, if present
        returnInst.getOperand().ifPresent(op -> code.append(apply(op)));
        code.append(typePrefix).append("return").append(NL);
        if(!typePrefix.isEmpty())
            stackWorker.pop();

        return code.toString();
    }

    private String generateInvokeSpecial(InvokeSpecialInstruction invokeInst) {
        var code = new StringBuilder();

        var target = invokeInst.getOperands().getFirst();
        code.append(apply(target));

        var params = new StringBuilder();
        for (var arg : invokeInst.getArguments()) {
            code.append(apply(arg));
            params.append(types.getTypeDescriptor(arg.getType()));
        }

        var className = getFullClassName(((ClassType) target.getType()).getName());
        var method = ((LiteralElement) invokeInst.getChildren().get(1)).getLiteral();

        // 5. Build the invokespecial instruction with the correct parameters
        code.append("invokespecial ").append(className.replace('.', '/'))
                .append("/").append(method).append("(").append(params).append(")V\n");
        stackWorker.pop();
        for(int i = 0; i < invokeInst.getArguments().size(); i++)
            stackWorker.pop();

        return code.toString();
    }

    private String generateNew(NewInstruction newInstruction) {
        var operand = newInstruction.getOperands().getFirst();
        var code = new StringBuilder();
        if(operand.getType() instanceof ClassType) {
            var name = ((ClassType) operand.getType()).getName();
            code.append("new ").append(getFullClassName(name)).append(NL);
            stackWorker.push();
        }
        else if(operand.getType() instanceof ArrayType array) { //It is an array
            var type = array.getElementType();
            String elemType = "";
            String newArray = "newarray ";
            if (type instanceof BuiltinType builtinType) {
                switch (builtinType.getKind()) {
                    case INT32 -> elemType = "int";
                    case BOOLEAN -> elemType = "boolean";
                    case VOID -> throw new RuntimeException("Unknown array type");
                    case STRING -> elemType = "java/lang/String";
                };
            }
            else if (type instanceof ClassType classType) {
                var name = classType.getName();
                elemType = getFullClassName(name);
                newArray = "anewarray ";
            }
            else {
                throw new RuntimeException("Unknown array type");
            }

            var ops = newInstruction.getOperands();
            for(int i = 1; i <= array.getNumDimensions(); i++)
                code.append(apply(ops.get(i)));

            for(int i = 0; i < array.getNumDimensions(); i++)
                stackWorker.pop();
            stackWorker.push();

            if(array.getNumDimensions() > 1) {
                newArray = "multianewarray ";
                elemType = "[".repeat(array.getNumDimensions()) + types.getTypeDescriptor(type)
                        + " " + array.getNumDimensions();
            }

            code.append(newArray).append(elemType).append(NL);
        }

        return code.toString();
    }


    private String generatePutFieldInst(PutFieldInstruction putFieldInst) {
        var ref = putFieldInst.getOperands().getFirst();
        var field = putFieldInst.getField();
        var val = putFieldInst.getOperands().getLast();
        var code = apply(ref) + apply(val) + "putfield " + types.getFullClassName(((ClassType) ref.getType()).getName()).replace('.', '/') +
                "/" + field.getName() + " " + types.getTypeDescriptor(field.getType()) +
                NL;
        stackWorker.pop();
        stackWorker.pop();
        return code;
    }

    private String generateGetFieldInst(GetFieldInstruction getFieldInst) {
        var ref = getFieldInst.getOperands().getFirst();
        var field = getFieldInst.getField();
        var code = apply(ref) + "getfield " + types.getFullClassName(((ClassType) ref.getType()).getName()) +
                "/" + field.getName() + " " + types.getTypeDescriptor(field.getType()) +
                NL;
        stackWorker.pop();
        stackWorker.push();
        return code;
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
        var code = apply(target) +
                "arraylength\n";
        stackWorker.pop();
        stackWorker.push();
        return code;
    }
}