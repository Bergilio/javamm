package core.semantics;

import org.junit.Test;
import pt.up.fe.comp.test.env.JmmTestEnv;


public class StaticMethodTest extends JmmTestEnv{
    private static final String BASE_PATH = "core/semantics/semantics/";
    private static final String RESOURCES_LOCATION = "test";

    public StaticMethodTest() {
        super(BASE_PATH, RESOURCES_LOCATION);
    }

    @Test
    public void useThisStaticMethod() {
        setDescription("Test that the use of 'this' inside static methods is rejected by the compiler");
        semantics("UseThisStaticMethod.jmm", true);
    }

    @Test
    public void useThisMethodStaticMethod() {
        setDescription("Test that the use of 'this.method()' inside static methods is rejected by the compiler");
        semantics("UseThisMethodStaticMethod.jmm", true);
    }

    @Test
    public  void staticMethodInstanceField() {
        setDescription("Test that static methods accessing instance fields is rejected by the compiler");
        semantics("StaticMethodInstanceField.jmm", true);
    }

}
