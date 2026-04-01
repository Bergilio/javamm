package core.semantics;

import org.junit.Test;
import pt.up.fe.comp.test.env.JmmTestEnv;

public class LocalCallsTest extends JmmTestEnv {

    private static final String BASE_PATH = "";
    private static final String RESOURCES_LOCATION = "test";

    public LocalCallsTest() {
        super(BASE_PATH, RESOURCES_LOCATION);
    }

    @Test
    public void testCallLocalNonStaticMethodFail() {
        setDescription("Test calling a local static method");
        semanticsFromSnippet("""
                package x;
                class N{
                    public int zero(){
                        return 0;
                    }
                    public static void main(String[] args) {
                        int x;
                        x = N.zero();
                    }
                }""", true);
    }

    @Test
    public void testCallLocalNonStaticMethod() {
        setDescription("Test calling a local static method");
        semanticsFromSnippet("""
                package x;
                class N{
                    public static int zero(){
                        return 0;
                    }
                    public static void main(String[] args) {
                        int x;
                        x = N.zero();
                    }
                }""", false);
    }

    @Test
    public void testFieldCallLocalFail() {
        setDescription("Test calling a local static method");
        semanticsFromSnippet("""
                package x;
                class N{
                    int a = 0;
                    public int zero(){
                        return 0;
                    }
                    public static void main(String[] args) {
                        int x;
                        x = N.a;
                    }
                }""", true);
    }

    @Test
    public void testFieldCallLocalFail2() {
        setDescription("Test calling a local static method");
        semanticsFromSnippet("""
                package x;
                class N{
                    int a = 0;
                    public int zero(){
                        return 0;
                    }
                    public static void main(String[] args) {
                        int x;
                        x = N.b;
                    }
                }""", true);
    }
    @Test
    public void testFieldCallLocal() {
        setDescription("Test calling a local static method");
        semanticsFromSnippet("""
                package x;
                class N{
                    int a = 0;
                    public int zero(){
                        return 0;
                    }
                    public static void main(String[] args) {
                        int x;
                        x = new N().a;
                    }
                }""", false);
    }
}
