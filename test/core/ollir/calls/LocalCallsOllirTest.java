package core.ollir.calls;

import org.junit.Test;
import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.LiteralElement;
import org.specs.comp.ollir.Method;
import org.specs.comp.ollir.inst.*;
import org.specs.comp.ollir.type.BuiltinKind;
import org.specs.comp.ollir.type.BuiltinType;
import pt.up.fe.comp.test.env.OllirTestEnv;

public class LocalCallsOllirTest extends OllirTestEnv {
    private static final String BASE_PATH = "core/ollir/calls/jmm/";
    private static final String RESOURCES_LOCATION = "test";

    public LocalCallsOllirTest() {
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
    public void testLocalInstanceCallRetInt() {
        var method = toOllir("LocalInstanceCallRetInt.jmm", "method");

        var calls = assertInstExists(InvokeVirtualInstruction.class, method);
        var call = calls.getFirst();
        assertTrue("Method name should be 'helper'",
                call.getMethodName() instanceof LiteralElement le && le.getLiteral().equals("helper"));
        assertTrue("Return type should be int",
                BuiltinType.is(call.getReturnType(), BuiltinKind.INT32));
    }

    @Test
    public void testLocalInstanceCallVoid() {
        var method = toOllir("LocalInstanceCallVoid.jmm", "method");

        var calls = assertInstExists(InvokeVirtualInstruction.class, method);
        var call = calls.getFirst();
        assertTrue("Method name should be 'helper'",
                call.getMethodName() instanceof LiteralElement le && le.getLiteral().equals("helper"));
        assertTrue("Return type should be void",
                BuiltinType.is(call.getReturnType(), BuiltinKind.VOID));
    }

    @Test
    public void testImplicitThisCallRetInt() {
        var method = toOllir("ImplicitThisCallRetInt.jmm", "method");

        var calls = assertInstExists(InvokeVirtualInstruction.class, method);
        var call = calls.getFirst();
        assertTrue("Method name should be 'helper'",
                call.getMethodName() instanceof LiteralElement le && le.getLiteral().equals("helper"));
        assertTrue("Return type should be int",
                BuiltinType.is(call.getReturnType(), BuiltinKind.INT32));
    }

    @Test
    public void testImplicitThisCallVoid() {
        var method = toOllir("ImplicitThisCallVoid.jmm", "method");

        var calls = assertInstExists(InvokeVirtualInstruction.class, method);
        var call = calls.getFirst();
        assertTrue("Method name should be 'helper'",
                call.getMethodName() instanceof LiteralElement le && le.getLiteral().equals("helper"));
        assertTrue("Return type should be void",
                BuiltinType.is(call.getReturnType(), BuiltinKind.VOID));
    }

    @Test
    public void testLocalStaticCallRetInt() {
        var method = toOllir("LocalStaticCallRetInt.jmm", "method");

        var calls = assertInstExists(InvokeStaticInstruction.class, method);
        var call = calls.getFirst();
        assertTrue("Method name should be 'helper'",
                call.getMethodName() instanceof LiteralElement le && le.getLiteral().equals("helper"));
        assertTrue("Return type should be int",
                BuiltinType.is(call.getReturnType(), BuiltinKind.INT32));
    }

    @Test
    public void testLocalCallWithArgs() {
        var method = toOllir("LocalCallWithArgs.jmm", "method");

        var calls = assertInstExists(InvokeVirtualInstruction.class, method);
        var call = calls.getFirst();
        assertTrue("Method name should be 'add'",
                call.getMethodName() instanceof LiteralElement le && le.getLiteral().equals("add"));
        assertEquals("Call should have 2 arguments", 2, call.getArguments().size());
        assertTrue("First argument should be int",
                BuiltinType.is(call.getArguments().get(0).getType(), BuiltinKind.INT32));
        assertTrue("Second argument should be int",
                BuiltinType.is(call.getArguments().get(1).getType(), BuiltinKind.INT32));
        assertTrue("Return type should be int",
                BuiltinType.is(call.getReturnType(), BuiltinKind.INT32));
    }

    @Test
    public void testNewInstanceCallNoArgs() {
        var method = toOllir("NewInstanceCallNoArgs.jmm", "method");

        // should have a new instruction followed by invokespecial then invokevirtual
        var news = assertInstExists(NewInstruction.class, method);
        var invokeSpecials = assertInstExists(InvokeSpecialInstruction.class, method);
        var calls = assertInstExists(InvokeVirtualInstruction.class, method);

        var call = calls.getFirst();
        assertTrue("Method name should be 'helper'",
                call.getMethodName() instanceof LiteralElement le && le.getLiteral().equals("helper"));
        assertTrue("Return type should be int",
                BuiltinType.is(call.getReturnType(), BuiltinKind.INT32));

        // new must come before the virtual call
        assertOrder(news.getFirst(), call, method);
    }

    @Test
    public void testNewInstanceCallWithArgs() {
        var method = toOllir("NewInstanceCallWithArgs.jmm", "method");

        var news = assertInstExists(NewInstruction.class, method);
        var calls = assertInstExists(InvokeVirtualInstruction.class, method);

        var call = calls.getFirst();
        assertTrue("Method name should be 'add'",
                call.getMethodName() instanceof LiteralElement le && le.getLiteral().equals("add"));
        assertEquals("Call should have 2 arguments", 2, call.getArguments().size());
        assertTrue("Return type should be int",
                BuiltinType.is(call.getReturnType(), BuiltinKind.INT32));

        assertOrder(news.getFirst(), call, method);
    }

    @Test
    public void testCallOnNewInstance() {
        var method = toOllir("CallOnNewInstance.jmm", "method");

        var news = assertInstExists(NewInstruction.class, method);
        var calls = assertInstExists(InvokeVirtualInstruction.class, method);

        var call = calls.getFirst();
        assertTrue("Method name should be 'helper'",
                call.getMethodName() instanceof LiteralElement le && le.getLiteral().equals("helper"));

        assertOrder(news.getFirst(), call, method);
    }
}