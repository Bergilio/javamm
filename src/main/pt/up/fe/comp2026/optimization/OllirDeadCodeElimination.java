package pt.up.fe.comp2026.optimization;

import org.specs.comp.ollir.*;
import org.specs.comp.ollir.inst.*;
import org.specs.comp.ollir.type.ArrayType;
import org.specs.comp.ollir.type.Type;

import java.util.*;

public class OllirDeadCodeElimination {
    private final Method method;

    private final Map<Instruction, Set<String>> liveIn = new LinkedHashMap<>();
    private final Map<Instruction, Set<String>> liveOut = new LinkedHashMap<>();

    public OllirDeadCodeElimination(Method method) {
        this.method = method;
    }

    public boolean simplify() {
        var localsAndParams = getLocalVarsAndParams();
        computeLiveness(localsAndParams);

        var toRemove = new ArrayList<Instruction>();

        for(var instruction : method.getInstructions())
            if(instruction.getInstType().equals(InstructionType.ASSIGN)) {
                var inst = (AssignInstruction) instruction;

                var outSet = liveOut.get(instruction);
                var op = ((Operand) inst.getDest()).getName();  //Expand to arrays

                boolean isDead = !outSet.contains(op);
                var rhs = inst.getRhs();
                boolean hasNoSideEffects = (rhs.getInstType() != InstructionType.CALL  || rhs instanceof NewInstruction)    //No possible side effects
                        && (!method.getVarTable().containsKey(op) || method.getVarTable().get(op).getScope() != VarScope.PARAMETER
                        || !(method.getVarTable().get(op).getVarType() instanceof ArrayType));  //Check if it is a parameter and,
                //if it is, make sure it isn't an array, else, there will be side effects because it was passed as a reference.

                if(isDead && hasNoSideEffects)
                    toRemove.add(instruction);

            }

        if (toRemove.isEmpty())
            return false;

        for(var instruction : toRemove)
            method.removeChild(instruction);

        return true;
    }

    private Set<String> getLocalVarsAndParams() {
        var locals = new LinkedHashSet<String>();
        for (var entry : method.getVarTable().entrySet()) {
            var scope = entry.getValue().getScope();
            if (scope == VarScope.LOCAL || scope == VarScope.PARAMETER)
                locals.add(entry.getKey());
        }
        return locals;
    }

    private void computeLiveness(Set<String> locals) {
        var instructions = method.getInstructions();

        liveIn.clear();
        liveOut.clear();

        for (var inst : instructions) {
            liveIn.put(inst, new HashSet<>());
            liveOut.put(inst, new HashSet<>());
        }

        boolean changed = true;
        while (changed) {
            changed = false;
            for (int i = instructions.size() - 1; i >= 0; i--) {
                var inst = instructions.get(i);

                var use = getUse(inst, locals);
                var def = getDef(inst, locals);

                var newOut = new HashSet<String>();
                for (var succ : inst.getSuccessorsAsInst()) {
                    newOut.addAll(liveIn.getOrDefault(succ, Set.of()));
                }

                var newIn = new HashSet<>(use);
                var outMinusDef = new HashSet<>(newOut);
                outMinusDef.removeAll(def);
                newIn.addAll(outMinusDef);

                if (!newIn.equals(liveIn.get(inst)) || !newOut.equals(liveOut.get(inst))) {
                    liveIn.put(inst, newIn);
                    liveOut.put(inst, newOut);
                    changed = true;
                }
            }
        }
    }

    private Set<String> getUse(Instruction inst, Set<String> locals) {
        var use = new HashSet<String>();
        if (inst instanceof AssignInstruction assign) {
            collectVars(assign.getRhs(), locals, use);
            if (assign.getDest() instanceof ArrayOperand arr) {
                if (locals.contains(arr.getName())) use.add(arr.getName());
                for (var idx : arr.getIndexOperands())
                    collectElementVars(idx, locals, use);
            }
        } else {
            collectVars(inst, locals, use);
        }
        return use;
    }

    private Set<String> getDef(Instruction inst, Set<String> locals) {
        var def = new HashSet<String>();
        if (inst instanceof AssignInstruction assign) {
            var dest = assign.getDest();
            if (dest instanceof Operand op && !(dest instanceof ArrayOperand)
                    && locals.contains(op.getName())) {
                def.add(op.getName());
            }
        }
        return def;
    }

    private void collectVars(Instruction inst, Set<String> locals, Set<String> result) {
        if (inst instanceof AssignInstruction assign) {
            collectVars(assign.getRhs(), locals, result);
        } else if (inst instanceof SingleOpInstruction soi) {
            collectElementVars(soi.getSingleOperand(), locals, result);
        } else if (inst instanceof BinaryOpInstruction bin) {
            collectElementVars(bin.getLeftOperand(), locals, result);
            collectElementVars(bin.getRightOperand(), locals, result);
        } else if (inst instanceof ReturnInstruction ret) {
            ret.getOperand().ifPresent(op -> collectElementVars(op, locals, result));
        } else if (inst instanceof CallInstruction call) {
            collectElementVars(call.getCaller(), locals, result);
            for (var arg : call.getArguments())
                collectElementVars(arg, locals, result);
        } else if (inst instanceof CondBranchInstruction cond) {
            collectVars(cond.getCondition(), locals, result);
        } else if (inst instanceof PutFieldInstruction putField) {
            collectElementVars(putField.getValue(), locals, result);
            collectElementVars(putField.getObject(), locals, result);
        } else if (inst instanceof GetFieldInstruction getField) {
            collectElementVars(getField.getObject(), locals, result);
        }
    }

    private void collectElementVars(Element elem, Set<String> locals, Set<String> result) {
        if (elem instanceof ArrayOperand arr) {
            if (locals.contains(arr.getName())) result.add(arr.getName());
            for (var idx : arr.getIndexOperands())
                collectElementVars(idx, locals, result);
        } else if (elem instanceof Operand op) {
            if (locals.contains(op.getName())) result.add(op.getName());
        }
    }
}
