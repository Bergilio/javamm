package pt.up.fe.comp2026.optimization;

import org.specs.comp.ollir.*;
import org.specs.comp.ollir.inst.*;

import java.util.*;

public class RegisterAllocator {

    private final Method method;
    private final int maxRegisters;

    private final Map<Instruction, Set<String>> liveIn = new LinkedHashMap<>();
    private final Map<Instruction, Set<String>> liveOut = new LinkedHashMap<>();
    private final Map<String, Set<String>> interference = new LinkedHashMap<>();

    public RegisterAllocator(Method method, int maxRegisters) {
        this.method = method;
        this.maxRegisters = maxRegisters;
    }

    public int allocate() {
        var locals = getLocalVars();
        if (locals.isEmpty()) return countCurrentRegisters();

        computeLiveness(locals);
        buildInterferenceGraph(locals);
        var coloring = colorGraph(locals);

        applyColoring(coloring);
        return coloring.isEmpty() ? countCurrentRegisters()
                : Collections.max(coloring.values()) + 1;
    }

    private Set<String> getLocalVars() {
        var locals = new LinkedHashSet<String>();
        for (var entry : method.getVarTable().entrySet()) {
            if (entry.getValue().getScope() == VarScope.LOCAL) {
                locals.add(entry.getKey());
            }
        }
        return locals;
    }

    private void computeLiveness(Set<String> locals) {
        var instructions = method.getInstructions();

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

    private void buildInterferenceGraph(Set<String> locals) {
        for (var v : locals) interference.put(v, new HashSet<>());

        for (var inst : method.getInstructions()) {
            var live = liveOut.get(inst);
            if (live == null) continue;

            if (inst instanceof AssignInstruction assign) {
                var dest = assign.getDest();
                if (dest instanceof Operand op && !(dest instanceof ArrayOperand)
                        && locals.contains(op.getName())) {
                    for (var v : live) {
                        if (!v.equals(op.getName())) {
                            addEdge(op.getName(), v);
                        }
                    }
                }
            }

            var liveList = new ArrayList<>(live);
            for (int i = 0; i < liveList.size(); i++) {
                for (int j = i + 1; j < liveList.size(); j++) {
                    addEdge(liveList.get(i), liveList.get(j));
                }
            }
        }
    }

    private void addEdge(String a, String b) {
        interference.computeIfAbsent(a, k -> new HashSet<>()).add(b);
        interference.computeIfAbsent(b, k -> new HashSet<>()).add(a);
    }

    private Map<String, Integer> colorGraph(Set<String> locals) {
        // -r=n means n colors for local variables (not total JVM slots)
        int effectiveColors = (maxRegisters == 0) ? locals.size() : maxRegisters;

        var remaining = new LinkedHashSet<>(locals);
        var stack = new ArrayDeque<String>();

        var graph = new HashMap<String, Set<String>>();
        for (var entry : interference.entrySet())
            graph.put(entry.getKey(), new HashSet<>(entry.getValue()));

        while (!remaining.isEmpty()) {
            String toRemove = null;
            for (var v : remaining) {
                long degree = graph.getOrDefault(v, Set.of()).stream()
                        .filter(remaining::contains).count();
                if (degree < effectiveColors) {
                    toRemove = v;
                    break;
                }
            }

            if (toRemove == null) {
                if (maxRegisters > 0) {
                    int minColors = computeMinColors(locals);
                    int paramOffset = method.isStaticMethod() ? 0 : 1;
                    int firstLocalSlot = paramOffset + method.getParams().size();
                    throw new RuntimeException(
                            "Not enough registers for method '" + method.getMethodName() +
                                    "'. Minimum required: " + (firstLocalSlot + minColors));
                }
                toRemove = remaining.iterator().next();
                effectiveColors++;
            }

            stack.push(toRemove);
            remaining.remove(toRemove);
        }

        var coloring = new HashMap<String, Integer>();
        while (!stack.isEmpty()) {
            var v = stack.pop();
            var neighbourColors = new HashSet<Integer>();
            for (var neighbour : interference.getOrDefault(v, Set.of())) {
                if (coloring.containsKey(neighbour))
                    neighbourColors.add(coloring.get(neighbour));
            }
            int color = 0;
            while (neighbourColors.contains(color)) color++;
            coloring.put(v, color);
        }

        return coloring;
    }

    private int computeMinColors(Set<String> locals) {
        var coloring = new HashMap<String, Integer>();
        for (var v : locals) {
            var neighbourColors = new HashSet<Integer>();
            for (var n : interference.getOrDefault(v, Set.of())) {
                if (coloring.containsKey(n)) neighbourColors.add(coloring.get(n));
            }
            int color = 0;
            while (neighbourColors.contains(color)) color++;
            coloring.put(v, color);
        }
        return coloring.isEmpty() ? 0 : Collections.max(coloring.values()) + 1;
    }

    private void applyColoring(Map<String, Integer> coloring) {
        int paramOffset = method.isStaticMethod() ? 0 : 1;
        int numParams = method.getParams().size();
        int firstLocalSlot = paramOffset + numParams;

        var varTable = method.getVarTable();
        for (var entry : coloring.entrySet()) {
            var descriptor = varTable.get(entry.getKey());
            if (descriptor != null) {
                descriptor.setVirtualReg(firstLocalSlot + entry.getValue());
            }
        }
    }

    private int countCurrentRegisters() {
        return (int) method.getVarTable().values().stream()
                .mapToInt(Descriptor::getVirtualReg)
                .distinct()
                .count();
    }
}