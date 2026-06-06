package core.ollir.optimization;

import org.junit.Test;
import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.Descriptor;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.test.env.OllirTestEnv;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;

public class RegAllocOptimalityTest extends OllirTestEnv {

    private static final String BASE_PATH = "pt/up/fe/comp/cp2/core/ollir/optimizations/";
    private static final String RESOURCES_LOCATION = "test-public";

    public RegAllocOptimalityTest() {
        super(BASE_PATH, RESOURCES_LOCATION);
    }

    @Test
    public void testRegAllocIf3_optimal() {
        var resourceName = "RegAllocIf3";
        var methodName   = "method";
        var isStatic     = false;
        var numParams    = 4;
        var rValueForLocals       = 3;
        // No "+1" allowance: the graph IS 3-colourable so 3 local slots must suffice.
        var maxRegistersForLocals = rValueForLocals;

        int expected_registers = 1 + numParams + maxRegistersForLocals; // = 8
        setDescription("Register allocation should use at most " + expected_registers
                + " registers with -r=" + rValueForLocals);

        var rValue = (isStatic ? 0 : 1) + numParams + rValueForLocals; // = 8
        setConfig(Map.of("registerAllocation", String.valueOf(rValue)));

        OllirResult ollirResult = loadOllir("ollir/" + resourceName + ".ollir");
        setComparingOllir(ollirResult.getOllirCode(), "Original Ollir", "Register Allocation",
                ollirResult.getOllirClass());
        ollirResult = optimize(ollirResult);

        ClassUnit classUnit = ollirResult.getOllirClass();
        var method = classUnit.getMethods().stream()
                .filter(m -> m.getMethodName().equals(methodName))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Method '" + methodName + "' not found"));

        int maxVirtualReg = method.getVarTable().values().stream()
                .mapToInt(Descriptor::getVirtualReg)
                .max().orElse(0);
        int actual = maxVirtualReg + 1;

        assertTrue(
            "Optimal allocation should use at most " + expected_registers
                + " register(s), but used " + actual
                + ". The simplification loop must select the minimum-degree node, "
                + "not just the first qualifying one.",
            actual <= expected_registers
        );
    }

    @Test
    public void testRegAllocIf2_optimal() {
        var resourceName = "RegAllocIf2";
        var methodName   = "method";
        var isStatic     = false;
        var numParams    = 4;
        var rValueForLocals       = 3;
        var maxRegistersForLocals = rValueForLocals;

        int expected_registers = 1 + numParams + maxRegistersForLocals; // = 8

        var rValue = (isStatic ? 0 : 1) + numParams + rValueForLocals;
        setConfig(Map.of("registerAllocation", String.valueOf(rValue)));

        OllirResult ollirResult = loadOllir("ollir/" + resourceName + ".ollir");
        setComparingOllir(ollirResult.getOllirCode(), "Original Ollir", "Register Allocation",
                ollirResult.getOllirClass());
        ollirResult = optimize(ollirResult);

        ClassUnit classUnit = ollirResult.getOllirClass();
        var method = classUnit.getMethods().stream()
                .filter(m -> m.getMethodName().equals(methodName))
                .findFirst()
                .orElseThrow();

        int maxVirtualReg = method.getVarTable().values().stream()
                .mapToInt(Descriptor::getVirtualReg)
                .max().orElse(0);
        int actual = maxVirtualReg + 1;

        assertTrue(
            "Optimal allocation should use at most " + expected_registers
                + " register(s), but used " + actual,
            actual <= expected_registers
        );
    }
}
