package core.semantics;

import org.junit.Test;
import pt.up.fe.comp.test.env.JmmTestEnv;


public class SuperClassTest extends JmmTestEnv{
    private static final String BASE_PATH = "core/semantics/semantics/";
    private static final String RESOURCES_LOCATION = "test";

    public SuperClassTest() {
        super(BASE_PATH, RESOURCES_LOCATION);
    }

    @Test
    public void ReturnThisAsSuperclassTest() {
        setDescription("Test that it is possible to return 'this' as a superclass");
        semantics("ReturnThisAsSuperclass.jmm", true);
    }

    @Test
    public void ClassNameInAssignmentTest() {
        setDescription("Test that is not possible to use a class name in the left hand of an assignment");
        semantics("ClassNameInAssignment.jmm", true);
    }

    @Test
    public void ReturnTypeOverrideTest() {
        setDescription("Test that the compiler fails when a method that overrides the one from the class it is extending changes the type of the return");
        semantics("InvalidReturnTypeOverride.jmm", true);
        setDescription("Test that the compiler does not fail when a method that overrides the one from the class it is extending does not change the type of the return");
        semantics("ValidReturnTypeOverride.jmm", false);
    }
}
