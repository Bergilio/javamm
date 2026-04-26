package core.ollir.calls;

import org.junit.Test;
import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.LiteralElement;
import org.specs.comp.ollir.Method;
import org.specs.comp.ollir.OperationType;
import org.specs.comp.ollir.inst.*;
import org.specs.comp.ollir.type.ArrayType;
import org.specs.comp.ollir.type.BuiltinKind;
import org.specs.comp.ollir.type.BuiltinType;
import pt.up.fe.comp.test.env.OllirTestEnv;

public class CallEdgeCasesOllirTest extends OllirTestEnv {
    private static final String BASE_PATH = "core/ollir/calls/jmm/";
    private static final String RESOURCES_LOCATION = "test";

    public CallEdgeCasesOllirTest() {
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
    public void testCallResultInExpr() {
        // return this.addOne(5) + 1 — call result used in binary expression
        var method = toOllir("CallEdgeCases.jmm", "callResultInExpr");

        var calls = assertInstExists(InvokeVirtualInstruction.class, method);
        assertTrue("Method name should be 'addOne'",
                calls.getFirst().getMethodName() instanceof LiteralElement le && le.getLiteral().equals("addOne"));
        assertAtLeast(method, BinaryOpInstruction.class, OperationType.ADD, 1);
    }

    @Test
    public void testCallAsArgument() {
        // return this.addOne(this.addOne(1)) — call result passed as argument
        var method = toOllir("CallEdgeCases.jmm", "callAsArgument");

        // should have exactly 2 invokevirtual calls to addOne
        var calls = assertInstExists(InvokeVirtualInstruction.class, method);
        assertEquals("Should have exactly 2 calls to addOne", 2, calls.size());
    }

    @Test
    public void testRecursiveCall() {
        // this.recursiveCall(n-1) — recursive call
        var method = toOllir("CallEdgeCases.jmm", "recursiveCall");

        assertInstExists(InvokeVirtualInstruction.class, method);
        assertInstExists(CondBranchInstruction.class, method);
    }

    @Test
    public void testMultipleCalls() {
        // three separate calls — ensures temporaries don't collide
        var method = toOllir("CallEdgeCases.jmm", "multipleCalls");

        var calls = assertInstExists(InvokeVirtualInstruction.class, method);
        assertEquals("Should have exactly 3 calls", 3, calls.size());
    }

    @Test
    public void testVoidCallAsStatement() {
        // this.doNothing() — void call as expression statement
        var method = toOllir("CallEdgeCases.jmm", "voidCallAsStatement");

        var calls = assertInstExists(InvokeVirtualInstruction.class, method);
        assertTrue("Method name should be 'doNothing'",
                calls.getFirst().getMethodName() instanceof LiteralElement le && le.getLiteral().equals("doNothing"));
        assertTrue("Return type should be void",
                BuiltinType.is(calls.getFirst().getReturnType(), BuiltinKind.VOID));
    }

    @Test
    public void testCallReturningBool() {
        // return this.isPositive(5) — call returning boolean
        var method = toOllir("CallEdgeCases.jmm", "callReturningBool");

        var calls = assertInstExists(InvokeVirtualInstruction.class, method);
        assertTrue("Return type should be boolean",
                BuiltinType.is(calls.getFirst().getReturnType(), BuiltinKind.BOOLEAN));
    }

    @Test
    public void testStaticCallResultInExpr() {
        // io.read() + 1 — static call result used in expression
        var method = toOllir("CallEdgeCases.jmm", "staticCallResultInExpr");

        assertInstExists(InvokeStaticInstruction.class, method);
        assertAtLeast(method, BinaryOpInstruction.class, OperationType.ADD, 1);
    }

    @Test
    public void testCallWithBooleanArg() {
        // this.consumeBool(flag) — call with boolean argument
        var method = toOllir("CallEdgeCases.jmm", "callWithBooleanArg");

        var calls = assertInstExists(InvokeVirtualInstruction.class, method);
        var call = calls.getFirst();
        assertEquals("Call should have 1 argument", 1, call.getArguments().size());
        assertTrue("Argument should be boolean",
                BuiltinType.is(call.getArguments().getFirst().getType(), BuiltinKind.BOOLEAN));
    }

    @Test
    public void testCallWithArrayArg() {
        // this.callWithArrayArg(arr) — call with array argument
        var method = toOllir("CallEdgeCases.jmm", "testCallWithArray");

        var calls = assertInstExists(InvokeVirtualInstruction.class, method);
        var call = calls.stream()
                .filter(c -> c.getMethodName() instanceof LiteralElement le && le.getLiteral().equals("callWithArrayArg"))
                .findFirst();
        assertTrue("Should have a call to callWithArrayArg", call.isPresent());
        assertEquals("Call should have 1 argument", 1, call.get().getArguments().size());
        assertTrue("Argument should be an array",
                call.get().getArguments().getFirst().getType() instanceof ArrayType);
    }

    @Test
    public void testCallReturningArray() {
        // arr = this.getArray(3) — call returning array
        var method = toOllir("CallEdgeCases.jmm", "testCallReturningArray");

        var calls = assertInstExists(InvokeVirtualInstruction.class, method);
        var call = calls.stream()
                .filter(c -> c.getMethodName() instanceof LiteralElement le && le.getLiteral().equals("getArray"))
                .findFirst();
        assertTrue("Should have a call to getArray", call.isPresent());
        assertTrue("Return type should be an array",
                call.get().getReturnType() instanceof ArrayType);
    }

    @Test
    public void testCallWithMixedArgs() {
        // this.mixedArgs(a, b) — call with int and boolean arguments
        var method = toOllir("CallEdgeCases.jmm", "testCallWithMixedArgs");

        var calls = assertInstExists(InvokeVirtualInstruction.class, method);
        var call = calls.stream()
                .filter(c -> c.getMethodName() instanceof LiteralElement le && le.getLiteral().equals("mixedArgs"))
                .findFirst();
        assertTrue("Should have a call to mixedArgs", call.isPresent());
        assertEquals("Call should have 2 arguments", 2, call.get().getArguments().size());
        assertTrue("First argument should be int",
                BuiltinType.is(call.get().getArguments().get(0).getType(), BuiltinKind.INT32));
        assertTrue("Second argument should be boolean",
                BuiltinType.is(call.get().getArguments().get(1).getType(), BuiltinKind.BOOLEAN));
    }

    @Test
    public void testChainedCall() {
        // this.getObj().addOne(1) — call on result of another call
        var method = toOllir("CallEdgeCases.jmm", "testChainedCall");

        var calls = assertInstExists(InvokeVirtualInstruction.class, method);
        assertTrue("Should have a call to getObj",
                calls.stream().anyMatch(c -> c.getMethodName() instanceof LiteralElement le && le.getLiteral().equals("getObj")));
        assertTrue("Should have a call to addOne",
                calls.stream().anyMatch(c -> c.getMethodName() instanceof LiteralElement le && le.getLiteral().equals("addOne")));

        var getObj = calls.stream()
                .filter(c -> c.getMethodName() instanceof LiteralElement le && le.getLiteral().equals("getObj"))
                .findFirst().orElseThrow();
        var addOne = calls.stream()
                .filter(c -> c.getMethodName() instanceof LiteralElement le && le.getLiteral().equals("addOne"))
                .findFirst().orElseThrow();
        assertOrder(getObj, addOne, method);
    }
}