package core.ollir.statements;

import org.junit.Test;
import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.Method;
import org.specs.comp.ollir.inst.AssignInstruction;
import org.specs.comp.ollir.inst.CondBranchInstruction;
import org.specs.comp.ollir.inst.GotoInstruction;
import org.specs.comp.ollir.inst.ReturnInstruction;
import pt.up.fe.comp.test.env.OllirTestEnv;

public class StatementsOllirTest extends OllirTestEnv {
    private static final String BASE_PATH = "core/ollir/statements/jmm/";
    private static final String RESOURCES_LOCATION = "test";

    public StatementsOllirTest() {
        super(BASE_PATH, RESOURCES_LOCATION);
    }

    public ClassUnit toOllir(String resourceName) {
        var ollirResult = jmmToOllir(resourceName);
        var classUnit = ollirResult.getOllirClass();
        assertNotNull("Ollir class unit should not be null", classUnit);
        assertEquals("Class name not what was expected", resourceName.replace(".jmm", ""), classUnit.getClassName());
        return classUnit;
    }

    public Method toOllir(String resourceName, String methodName) {
        var classUnit = toOllir(resourceName);
        var method = classUnit.getMethods().stream()
                .filter(m -> m.getMethodName().equals(methodName))
                .findFirst();
        assertTrue("Method '" + methodName + "' should exist", method.isPresent());
        return method.orElseThrow();
    }

    @Test
    public void testSimpleWhile() {
        var method = toOllir("SimpleWhile.jmm", "method");

        assertInstExists(CondBranchInstruction.class, method);
        assertAtLeast(GotoInstruction.class, 1, method);
        assertInstExists(ReturnInstruction.class, method);
    }

    @Test
    public void testNestedWhile() {
        var method = toOllir("NestedWhile.jmm", "method");

        assertAtLeast(CondBranchInstruction.class, 2, method);
        assertAtLeast(GotoInstruction.class, 2, method);
    }

    @Test
    public void testSimpleIfElse() {
        var method = toOllir("SimpleIfElse.jmm", "method");

        assertInstExists(CondBranchInstruction.class, method);
        assertInstExists(GotoInstruction.class, method);
        assertInstExists(ReturnInstruction.class, method);

        var assigns = assertInstExists(AssignInstruction.class, method);
        assertTrue("Should have at least 2 assignments", assigns.size() >= 2);
    }

    @Test
    public void testSimpleIfNoElse() {
        var method = toOllir("SimpleIfNoElse.jmm", "method");

        assertInstExists(CondBranchInstruction.class, method);
        assertInstExists(ReturnInstruction.class, method);
    }

    @Test
    public void testForLoop() {
        var method = toOllir("ForLoop.jmm", "method");

        assertInstExists(CondBranchInstruction.class, method);
        assertAtLeast(GotoInstruction.class, 1, method);
        assertInstExists(ReturnInstruction.class, method);
    }

    @Test
    public void testDoWhileLoop() {
        var method = toOllir("DoWhileLoop.jmm", "method");

        assertInstExists(CondBranchInstruction.class, method);
        assertAtLeast(GotoInstruction.class, 1, method);
        assertInstExists(ReturnInstruction.class, method);
    }

    @Test
    public void testNestedIf() {
        var method = toOllir("NestedIf.jmm", "method");

        assertAtLeast(CondBranchInstruction.class, 2, method);
        assertAtLeast(GotoInstruction.class, 2, method);
        assertInstExists(ReturnInstruction.class, method);
    }

    @Test
    public void testIfInsideWhile() {
        var method = toOllir("IfInsideWhile.jmm", "method");

        assertAtLeast(CondBranchInstruction.class, 2, method);
        assertAtLeast(GotoInstruction.class, 2, method);
        assertInstExists(ReturnInstruction.class, method);
    }

    @Test
    public void testBlockStatement() {
        var method = toOllir("BlockStatement.jmm", "method");

        assertInstExists(CondBranchInstruction.class, method);
        var assigns = assertInstExists(AssignInstruction.class, method);
        assertTrue("Should have at least 2 assignments inside block", assigns.size() >= 2);
        assertInstExists(ReturnInstruction.class, method);
    }
}