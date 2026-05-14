package core.semantics;

import org.junit.Test;
import pt.up.fe.comp.test.env.JmmTestEnv;


public class ReturnThisTest extends JmmTestEnv{
    private static final String BASE_PATH = "core/semantics/semantics/";
    private static final String RESOURCES_LOCATION = "test";

    public ReturnThisTest() {
        super(BASE_PATH, RESOURCES_LOCATION);
    }

    @Test
    public void RetrunThisAsSuperclassTest() {
        setDescription("Test that it is possible to return 'this' as a superclass");
        semantics("ReturnThisAsSuperclass.jmm", true);
    }

}
