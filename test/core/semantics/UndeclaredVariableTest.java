package core.semantics;

import org.junit.Test;
import pt.up.fe.comp.test.env.JmmTestEnv;

public class UndeclaredVariableTest extends JmmTestEnv {
    private static final String BASE_PATH = "core/semantics/semantics/";
    private static final String RESOURCES_LOCATION = "test";

    public UndeclaredVariableTest() {
        super(BASE_PATH, RESOURCES_LOCATION);
    }

    @Test
    public void testUndeclaredVariable() {
        setDescription("Test the UndeclaredVariablePass");
        semantics("DeclaredVariables.jmm", false);
        semantics("UndeclaredVariables.jmm", true);
    }
}
