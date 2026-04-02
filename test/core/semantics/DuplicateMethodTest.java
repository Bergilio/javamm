package core.semantics;

import org.junit.Test;
import pt.up.fe.comp.test.env.JmmTestEnv;

public class DuplicateMethodTest extends JmmTestEnv {
    private static final String BASE_PATH = "";
    private static final String RESOURCES_LOCATION = "test";

    public DuplicateMethodTest() {
        super(BASE_PATH, RESOURCES_LOCATION);
    }

    @Test
    public void testMethodOverloading() {
        setDescription("Test method overloading");
        semanticsFromSnippet("""
                package doesnt.matter;
                class Test {
                    public int test(int n){
                        return 0;
                    }
                    public int test(int n){
                        return 0;
                    }
                }""", true);
        semanticsFromSnippet("""
                package doesnt.matter;
                class Test {
                    public int test(int n){
                        return 0;
                    }
                    public int test(boolean n){
                        return 0;
                    }
                }""", false);
        semanticsFromSnippet("""
                package doesnt.matter;
                class Test {
                    public int test(int n){
                        return 0;
                    }
                    public boolean test(int n){
                        return true;
                    }
                }""", true);
        semanticsFromSnippet("""
                package doesnt.matter;
                class Test {
                    public int test(int n){
                        return 0;
                    }
                    public boolean test(boolean n){
                        return true;
                    }
                }""", false);
    }
}
