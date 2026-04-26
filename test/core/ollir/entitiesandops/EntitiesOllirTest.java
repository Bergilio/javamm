package core.ollir.entitiesandops;

import org.junit.Test;
import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.Method;
import org.specs.comp.ollir.inst.*;
import org.specs.comp.ollir.type.BuiltinKind;
import org.specs.comp.ollir.type.BuiltinType;
import pt.up.fe.comp.test.env.OllirTestEnv;

public class EntitiesOllirTest extends OllirTestEnv {
    private static final String BASE_PATH = "core/ollir/entitiesandops/jmm/";
    private static final String RESOURCES_LOCATION = "test";

    public EntitiesOllirTest() {
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
    public void testGetFieldViaThis() {
        var method = toOllir("FieldAccess.jmm", "getFieldViaThis");

        assertInstExists(GetFieldInstruction.class, method);
        assertInstExists(ReturnInstruction.class, method);
    }

    @Test
    public void testGetFieldViaObj() {
        var method = toOllir("FieldAccess.jmm", "getFieldViaObj");

        assertInstExists(GetFieldInstruction.class, method);
        assertInstExists(ReturnInstruction.class, method);
    }

    @Test
    public void testSetField() {
        var method = toOllir("FieldAccess.jmm", "setField");

        assertInstExists(PutFieldInstruction.class, method);
    }

    @Test
    public void testFieldInExpression() {
        var method = toOllir("FieldAccess.jmm", "fieldInExpression");

        assertInstExists(GetFieldInstruction.class, method);
        assertAtLeast(method, BinaryOpInstruction.class, org.specs.comp.ollir.OperationType.ADD, 1);
    }

    @Test
    public void testFieldAssignFromExpression() {
        var method = toOllir("FieldAccess.jmm", "fieldAssignFromExpression");

        assertInstExists(GetFieldInstruction.class, method);
        assertInstExists(PutFieldInstruction.class, method);
    }

    @Test
    public void testVoidCallOnObject() {
        var method = toOllir("VoidFunctionCall.jmm", "method");

        assertInstExists(NewInstruction.class, method);
        var calls = assertInstExists(InvokeVirtualInstruction.class, method);
        assertTrue("Should have a void call to doSomething",
                calls.stream().anyMatch(c ->
                        c.getMethodName() instanceof org.specs.comp.ollir.LiteralElement le &&
                        le.getLiteral().equals("doSomething") &&
                        BuiltinType.is(c.getReturnType(), BuiltinKind.VOID)));
    }

    @Test
    public void testStaticVoidCall() {
        var method = toOllir("VoidFunctionCall.jmm", "staticVoidCall");

        var calls = assertInstExists(InvokeStaticInstruction.class, method);
        assertTrue("Should have a void call to print",
                calls.stream().anyMatch(c ->
                        c.getMethodName() instanceof org.specs.comp.ollir.LiteralElement le &&
                        le.getLiteral().equals("print") &&
                        BuiltinType.is(c.getReturnType(), BuiltinKind.VOID)));
    }
}
