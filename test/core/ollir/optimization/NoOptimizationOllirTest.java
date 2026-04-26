package core.ollir.optimization;

import org.junit.Test;
import org.specs.comp.ollir.Method;
import org.specs.comp.ollir.inst.BinaryOpInstruction;
import pt.up.fe.comp.test.env.OllirTestEnv;

/**
 * Tests without -o flag — verifies that without optimization,
 * binary operations are NOT folded (i.e., BinaryOpInstructions exist).
 */
public class NoOptimizationOllirTest extends OllirTestEnv {
    private static final String BASE_PATH = "core/ollir/optimization/jmm/";
    private static final String RESOURCES_LOCATION = "test";

    public NoOptimizationOllirTest() {
        super(BASE_PATH, RESOURCES_LOCATION);
    }

    private Method getMethod(String resource, String methodName) {
        var ollirResult = jmmToOllir(resource);
        var classUnit = ollirResult.getOllirClass();
        return getMethod(ollirResult, methodName);
    }

    @Test
    public void testArithmeticNotFolded() {
        var method = getMethod("ConstantFoldingArithmetic.jmm", "simpleAdd");
        assertInstExists(BinaryOpInstruction.class, method);
    }

    @Test
    public void testBooleanComparisonNotFolded() {
        var method = getMethod("ConstantFoldingBoolean.jmm", "comparison");
        assertInstExists(BinaryOpInstruction.class, method);
    }

    @Test
    public void testPropagationNotApplied() {
        var method = getMethod("ConstantPropagation.jmm", "simplePropagate");
        assertInstExists(BinaryOpInstruction.class, method);
    }
}
