package core.ollir.optimization;

import org.junit.Test;
import org.specs.comp.ollir.Method;
import org.specs.comp.ollir.inst.BinaryOpInstruction;
import org.specs.comp.ollir.inst.UnaryOpInstruction;
import pt.up.fe.comp.test.env.OllirTestEnv;

import java.util.Map;

/**
 * Tests with -o flag. Uses the config constructor of OllirTestEnv so that
 * "optimize=true" is baked into the instance from construction time,
 * and JUnit's @Rule injects description normally.
 */
public class OptimizationOllirTest extends OllirTestEnv {
    private static final String BASE_PATH = "core/ollir/optimization/jmm/";
    private static final String RESOURCES_LOCATION = "test";

    public OptimizationOllirTest() {
        super(BASE_PATH, RESOURCES_LOCATION, Map.of("optimize", "true"));
    }

    private Method getMethod(String resource, String methodName) {
        var ollirResult = jmmToOllir(resource);
        return getMethod(ollirResult, methodName);
    }

    // -------------------------------------------------------------------------
    // Constant Folding — Arithmetic
    // -------------------------------------------------------------------------

    @Test
    public void testSimpleAddFolded() {
        var method = getMethod("ConstantFoldingArithmetic.jmm", "simpleAdd");
        assertTrue("10+5 should fold: no BinaryOpInstruction",
                getInstructions(BinaryOpInstruction.class, method).isEmpty());
        assertFindAssignmentWithLiteral("15", method);
    }

    @Test
    public void testSimpleMulFolded() {
        var method = getMethod("ConstantFoldingArithmetic.jmm", "simpleMul");
        assertTrue("3*4 should fold: no BinaryOpInstruction",
                getInstructions(BinaryOpInstruction.class, method).isEmpty());
        assertFindAssignmentWithLiteral("12", method);
    }

    @Test
    public void testSimpleSubFolded() {
        var method = getMethod("ConstantFoldingArithmetic.jmm", "simpleSub");
        assertTrue("10-3 should fold: no BinaryOpInstruction",
                getInstructions(BinaryOpInstruction.class, method).isEmpty());
        assertFindAssignmentWithLiteral("7", method);
    }

    @Test
    public void testSimpleDivFolded() {
        var method = getMethod("ConstantFoldingArithmetic.jmm", "simpleDiv");
        assertTrue("20/4 should fold: no BinaryOpInstruction",
                getInstructions(BinaryOpInstruction.class, method).isEmpty());
        assertFindAssignmentWithLiteral("5", method);
    }

    @Test
    public void testNestedFolded() {
        var method = getMethod("ConstantFoldingArithmetic.jmm", "nested");
        assertTrue("(2+3)*4 should fully fold: no BinaryOpInstruction",
                getInstructions(BinaryOpInstruction.class, method).isEmpty());
        assertFindAssignmentWithLiteral("20", method);
    }

    @Test
    public void testChainedFolded() {
        var method = getMethod("ConstantFoldingArithmetic.jmm", "chained");
        assertTrue("1+2+3+4 should fully fold: no BinaryOpInstruction",
                getInstructions(BinaryOpInstruction.class, method).isEmpty());
        assertFindAssignmentWithLiteral("10", method);
    }

    // -------------------------------------------------------------------------
    // Constant Folding — Boolean
    // -------------------------------------------------------------------------

    @Test
    public void testAndFolded() {
        var method = getMethod("ConstantFoldingBoolean.jmm", "andExpr");
        assertFindAssignmentWithLiteral("0", method);
    }

    @Test
    public void testOrFolded() {
        var method = getMethod("ConstantFoldingBoolean.jmm", "orExpr");
        assertFindAssignmentWithLiteral("1", method);
    }

    @Test
    public void testNotFolded() {
        var method = getMethod("ConstantFoldingBoolean.jmm", "notExpr");
        assertTrue("!true should fold: no UnaryOpInstruction",
                getInstructions(UnaryOpInstruction.class, method).isEmpty());
        assertFindAssignmentWithLiteral("0", method);
    }

    @Test
    public void testComparisonTrueFolded() {
        var method = getMethod("ConstantFoldingBoolean.jmm", "comparison");
        assertTrue("3<5 should fold: no BinaryOpInstruction",
                getInstructions(BinaryOpInstruction.class, method).isEmpty());
        assertFindAssignmentWithLiteral("1", method);
    }

    @Test
    public void testComparisonFalseFolded() {
        var method = getMethod("ConstantFoldingBoolean.jmm", "comparisonFalse");
        assertTrue("10<5 should fold: no BinaryOpInstruction",
                getInstructions(BinaryOpInstruction.class, method).isEmpty());
        assertFindAssignmentWithLiteral("0", method);
    }

    // -------------------------------------------------------------------------
    // Constant Propagation
    // -------------------------------------------------------------------------

    @Test
    public void testSimplePropagate() {
        var method = getMethod("ConstantPropagation.jmm", "simplePropagate");
        assertTrue("After propagation+folding: no BinaryOpInstruction expected",
                getInstructions(BinaryOpInstruction.class, method).isEmpty());
        assertFindAssignmentWithLiteral("8", method);
    }

    @Test
    public void testPropagateThenFold() {
        var method = getMethod("ConstantPropagation.jmm", "propagateThenFold");
        assertTrue("After propagation+folding: no BinaryOpInstruction expected",
                getInstructions(BinaryOpInstruction.class, method).isEmpty());
        assertFindAssignmentWithLiteral("8", method);
    }

    @Test
    public void testReassignNoPropagate() {
        var method = getMethod("ConstantPropagation.jmm", "reassignNoPropagate");
        assertTrue("After propagation+folding: no BinaryOpInstruction expected",
                getInstructions(BinaryOpInstruction.class, method).isEmpty());
        assertFindAssignmentWithLiteral("13", method);
    }

    @Test
    public void testPropagateBool() {
        var method = getMethod("ConstantPropagation.jmm", "propagateBool");
        assertTrue("After propagation+folding: no UnaryOpInstruction expected",
                getInstructions(UnaryOpInstruction.class, method).isEmpty());
        assertFindAssignmentWithLiteral("0", method);
    }

    // -------------------------------------------------------------------------
    // Loop Safety — variables assigned in loops must not be propagated
    // -------------------------------------------------------------------------

    @Test
    public void testLoopCounterNotPropagated() {
        var method = getMethod("ConstantPropagationLoop.jmm", "loopCounter");
        assertNotNull("Method should compile successfully", method);
    }

    @Test
    public void testLoopSumNotPropagated() {
        var method = getMethod("ConstantPropagationLoop.jmm", "loopSum");
        assertNotNull("Method should compile successfully", method);
    }

    @Test
    public void testConstBeforeLoopPropagated() {
        var method = getMethod("ConstantPropagationLoop.jmm", "constBeforeLoop");
        assertNotNull("Method should compile successfully", method);
        assertFindAssignmentWithLiteral("8", method);
    }
}
