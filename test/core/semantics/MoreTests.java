package core.semantics;

import org.junit.Test;

public class MoreTests extends pt.up.fe.comp.test.env.JmmTestEnv {
    private static final String BASE_PATH = "core/semantics/semantics";
    private static final String RESOURCES_LOCATION = "test";

    public MoreTests() {
        super(BASE_PATH, RESOURCES_LOCATION);
    }

    @Test
    public void implicitThisRecursion() {
        setDescription("A method calls itself without this");
        semanticsFromSnippet("""
              package doesnt.matter;

              class ImplicitRecursive {
                    int count(int n) {
                        int res;
                        if (n < 1) { res = 0; }
                        else { res = 1 + count(n - 1); } // Implicit this
                        return res;
                    }
              }""", false);
        semanticsFromSnippet("""
              package doesnt.matter;

              class ImplicitRecursive {
                    int count(int n) {
                        int res;
                        if (n < 1) { res = 0; }
                        else { res = 1 + counter(n - 1); } // Implicit this
                        return res;
                    }
              }""", true);
    }

    @Test
    public void multiDimDepthMatch() {
        setDescription("An 1D array is assigned to a slot of a 2D array");
        semanticsFromSnippet("""
                package doesnt.matter;
                class MultiDimDepth {
                    int[][] matrix;
                    int test() {
                        int[] row;
                        matrix = new int[2][3];
                        row = new int[3];
                        matrix[0] = row; // Correct: matrix[0] is int[]
                        return matrix[0][0];
                    }
                }""", false);
        semanticsFromSnippet("""
                package doesnt.matter;
                class MultiDimDepth {
                    int[][] matrix;
                    int test() {
                        int row;
                        matrix = new int[2][3];
                        row = 10;
                        matrix[0] = row; // Correct: matrix[0] is int[]
                        return matrix[0][0];
                    }
                }""", true);
    }

    @Test
    public void arrayOps() {
        setDescription("An array can't be used in arithmetic ops");
        semanticsFromSnippet("""
                package doesnt.matter;
                class MultiDimDepth {
                    int[][] matrix;
                    int[][] a;
                    int test() {
                        matrix = new int[2][3];
                        matrix = matrix + a;
                        return matrix[0][0];
                    }
                }""", true);
        semanticsFromSnippet("""
                package doesnt.matter;
                class MultiDimDepth {
                    int[][] matrix;
                    int[][] a;
                    int test() {
                        matrix = new int[2][3];
                        a = new int[2][3];
                        matrix[0] = matrix[0] + a[0];
                        return matrix[0][0];
                    }
                }""", true);
        semanticsFromSnippet("""
                package doesnt.matter;
                class MultiDimDepth {
                    int[][] matrix;
                    int[][] a;
                    int test() {
                        matrix = new int[2][3];
                        a = new int[2][3];
                        matrix[0][0] = matrix[0][0] + a[0][0];
                        return matrix[0][0];
                    }
                }""", false);
        semanticsFromSnippet("""
                package doesnt.matter;
                class MultiDimDepth {
                    int a;
                    int test() {
                        a[0] = 2;
                    }
                }""", true);   //Checking access
    }

    @Test
    public void assignToSuper() {
        setDescription("Object is assigned to super");
        semanticsFromSnippet("""
                package doesnt.matter;
                import examples.inheritance.A;
                import examples.inheritance.B;
                
                class In {
                    A obj;
                    int test() {
                        obj = new B(); // B extends A
                        return obj.a();
                    }
                }""", false);
        semanticsFromSnippet("""
                package doesnt.matter;
                import examples.inheritance.A;
                import examples.inheritance.B;
                
                class In {
                    B obj;
                    int test() {
                        obj = new A(); // A doesn't extend B
                        return obj.a();
                    }
                }""", true);
        semanticsFromSnippet("""
                package doesnt.matter;
                import examples.inheritance.A;
                import examples.inheritance.B;
                
                class In {
                    Object obj;
                    int test() {
                        obj = new B(); // B extends Object by Default
                        return obj.a(); //Obj should not have .a(), as that's from B
                    }
                }""", true);
        semanticsFromSnippet("""
                package doesnt.matter;
                import examples.inheritance.A;
                import examples.inheritance.B;
                
                class In {
                    Object obj;
                    int test() {
                        obj = new B(); // B extends Object by Default
                        return obj.hashCode(); //Obj should not have .a(), as that's from B
                    }
                }""", false);
        semanticsFromSnippet("""
                package doesnt.matter;
                import examples.inheritance.A;
                import examples.inheritance.B;
                
                class In {
                    Object obj;
                    int test() {
                        obj = new In(); // In extends Object by Default (omission)
                        return obj.hashCode();
                    }
                }""", false);
    }

    @Test
    public void boolExpr() {
        setDescription("Mix of logical ops and method calls");
        semanticsFromSnippet("""
                package doesnt.matter;
                import examples.Auxi;
                
                class LogicalMix {
                    boolean check(Auxi a) {
                        return (1 < 2) || a.instanceCallBool();
                    }
                }""", false);
        semanticsFromSnippet("""
                package doesnt.matter;
                import examples.Auxi;
                
                class LogicalMix {
                    boolean check(Auxi a) {
                        return (1 < 2) || a.instanceCallInt();
                    }
                }""", true);
        semanticsFromSnippet("""
                package doesnt.matter;
                import examples.Auxi;
                
                class LogicalMix {
                    boolean check(Auxi a) {
                        return (1 < 2) || a.instanceCallVoid();
                    }
                }""", true);
    }

    @Test
    public void arrayInitExpr() {
        setDescription("Array initializer with methods, literals, etc.");
        semanticsFromSnippet("""
                package doesnt.matter;
                import examples.Auxi;
                
                class ArrayInitExpr {
                    int[] test(Auxi a) {
                        return new int[]{ 1, a.instanceCallInt(), 3 + 4 };
                    }
                }""", false);
        semanticsFromSnippet("""
                package doesnt.matter;
                import examples.Auxi;
                
                class ArrayInitExpr {
                    int[] test(Auxi a) {
                        return new int[]{ 1, a.instanceCallBool(), 3 + 4 };
                    }
                }""", true);
        semanticsFromSnippet("""
                package doesnt.matter;
                import examples.Auxi;
                
                class ArrayInitExpr {
                    boolean[] test(Auxi a) {
                        return new boolean[]{ true, a.instanceCallBool(), true && false };
                    }
                }""", false);
        semanticsFromSnippet("""
                package doesnt.matter;
                import examples.Auxi;
                
                class ArrayInitExpr {
                    boolean[] test(Auxi a) {
                        return new Boolean[]{ false, a.instanceCallBool(), 3 + 4 };
                    }
                }""", true);
        semanticsFromSnippet("""
                package doesnt.matter;
                import examples.Auxi;
                
                class ArrayInitExpr {
                    Object[] test(Auxi a) {
                        return new Object[]{ a, new ArrayInitExpr(), new Auxi() };
                    }
                }""", false);
        semanticsFromSnippet("""
                package doesnt.matter;
                import examples.Auxi;
                
                class ArrayInitExpr {
                    Object[] test(Auxi a) {
                        return new Object[]{ a, new ArrayInitExpr(), 10 };
                    }
                }""", true);
        semanticsFromSnippet("""
                package doesnt.matter;
                
                class MultiDimNew {
                    int[][][] cube;
                    void init() {
                        cube = new int[2][2][2];
                    }
                }""", false);
        semanticsFromSnippet("""
                package doesnt.matter;
                
                class MultiDimNew {
                    int[][][] cube;
                    void init() {
                        cube = new int[2][true][2];
                    }
                }""", true);
    }

    @Test
    public void nestedCalls() {
        setDescription("Nested calls of method");
        semanticsFromSnippet("""
                package doesnt.matter;
                import examples.Pair;
                
                class ChainedCall {
                    int test() {
                        return new Pair(10, 20).getLeft();
                    }
                }""", false);
        semanticsFromSnippet("""
                package doesnt.matter;
                import examples.Pair;
                
                class ChainedCall {
                    int test() {
                        return new Pair(10).getLeft();
                    }
                }""", true);
        semanticsFromSnippet("""
                package doesnt.matter;
                import examples.Pair;
                
                class ChainedCall {
                    Pair a = new Pair(10,20);
                    int test() {
                        return new ChainedCall().a.getLeft();
                    }
                }""", false);
        semanticsFromSnippet("""
                package doesnt.matter;
                import examples.Pair;
                
                class ChainedCall {
                    Pair a = new Pair();
                    int test() {
                        return new ChainedCall().a.getLeft();
                    }
                }""", true);
    }

    @Test
    public void forTest() {
        setDescription("For loop that changes fields");
        semanticsFromSnippet("""
                package doesnt.matter;
                
                class ForField {
                    int i;
                    int run() {
                        int sum;
                        sum = 0;
                        for(i = 0; i < 10; i = i + 1) {
                            sum = sum + i;
                        }
                        return sum;
                    }
                }""", false);
        semanticsFromSnippet("""
                package doesnt.matter;
                
                class ForField {
                    int i;
                    int run() {
                        int sum;
                        sum = 0;
                        for(i = 0; i; i = i + 1) {
                            sum = sum + i;
                        }
                        return sum;
                    }
                }""", true);
        semanticsFromSnippet("""
                package doesnt.matter;
                
                class ForField {
                    boolean i;
                    int run() {
                        int sum;
                        sum = 0;
                        for(i = true; i;)
                            sum = sum + 1;
                        return sum;
                    }
                }""", false);
        semanticsFromSnippet("""
                package doesnt.matter;
                
                class ForField {
                    boolean i;
                    int run() {
                        int sum;
                        sum = 0;
                        for(i = true; i;)
                            sum = sum + i;
                        return sum;
                    }
                }""", true);
    }

    @Test
    public void nestedArrays() {
        setDescription("Using an array element as index for another array access");
        semanticsFromSnippet("""
                package doesnt.matter;
                
                class NestedArrayIndex {
                    int test(int[] a, int[] b) {
                        return a[b[0]];
                    }
                }""", false);
        semanticsFromSnippet("""
                package doesnt.matter;
                
                class NestedArrayIndex {
                    int test(int[] a, bool[] b) {
                        return a[b[0]];
                    }
                }""", true);
        semanticsFromSnippet("""
                package doesnt.matter;
                
                class NestedArrayIndex {
                    int test(int[] a, int[] b) {
                        return a[b];
                    }
                }""", true);
        semanticsFromSnippet("""
                package doesnt.matter;
                
                class NestedArrayIndex {
                    int test(int[] a, int[][] b) {
                        return a[b[0]];
                    }
                }""", true);
        semanticsFromSnippet("""
                package doesnt.matter;
                
                class NestedArrayIndex {
                    int test(int[] a, int[][] b) {
                        return a[b[0][0]];
                    }
                }""", false);
    }

    @Test
    public void returnsTest() {
        setDescription("A returning method, can't return the result of a non-matching return method");
        semanticsFromSnippet("""
                package doesnt.matter;
                import examples.Auxi;
                
                class Trap {
                    int test(Auxi a) {
                        return a.instanceCallInt();
                    }
                }""", false);
        semanticsFromSnippet("""
                package doesnt.matter;
                import examples.Auxi;
                
                class Trap {
                    int test(Auxi a) {
                        return a.instanceCallVoid();
                    }
                }""", true);
        semanticsFromSnippet("""
                package doesnt.matter;
                import examples.Auxi;
                
                class Trap {
                    int test(Auxi a) {
                        return a.instanceCallBool();
                    }
                }""", true);
        semanticsFromSnippet("""
                package doesnt.matter;
                import examples.Auxi;
                
                class Trap {
                    void test(Auxi a) {
                        return a.instanceCallVoid();
                    }
                }""", true);
        semanticsFromSnippet("""
                package doesnt.matter;
                import examples.Auxi;
                
                class Trap {
                    void test(Auxi a) {
                        a.instanceCallVoid();
                        return;
                    }
                }""", false);
    }

    @Test
    public void loopOrLogicTests() {
        setDescription("Tests on loops");
        semanticsFromSnippet("""
                package doesnt.matter;
                
                class IfBranch {
                    int test() {
                        int x;
                        if (1 < 2) { x = 1; }
                        else { x = 2; }
                        return x;
                    }
                }""", false);
        semanticsFromSnippet("""
                package doesnt.matter;
                
                class IfBranch {
                    int test() {
                        int x;
                        if (1 < 2) { x = 1; }
                        return x;
                    }
                }""", false);
        semanticsFromSnippet("""
                package doesnt.matter;
                
                class IfBranch {
                    int test() {
                        int x;
                        if (1) { x = 1; }
                        return x;
                    }
                }""", true);
        semanticsFromSnippet("""
                package doesnt.matter;
                
                class WhileComplex {
                    void loop(int[] a) {
                        int i;
                        i = 0;
                        while( (i < 10) && (a[i] < 100) ) {
                            i = i + 1;
                        }
                    }
                }""", false);
        semanticsFromSnippet("""
                package doesnt.matter;
                
                class WhileComplex {
                    void loop(int[] a) {
                        int i;
                        i = 0;
                        while(a[i]) {
                            i = i + 1;
                        }
                    }
                }""", true);
    }

    @Test
    public void implicitFields() {
        setDescription("Tests on implicit field access");
        semanticsFromSnippet("""
                package doesnt.matter;
                
                class ImplicitField {
                    int width;
                    int getArea(int height) {
                        return width * height;
                    }
                }""", false);
        semanticsFromSnippet("""
                package doesnt.matter;
                
                class ImplicitField {
                    int width;
                    int height;
                    int getArea() {
                        return width * height;
                    }
                }""", false);
        semanticsFromSnippet("""
                package doesnt.matter;
                
                class ImplicitField {
                    int width;
                    int getArea(int height) {
                        return width * height * true;
                    }
                }""", true);
    }

    @Test
    public void returnsOnStatic() {
        setDescription("Method returns result of static call");
        semanticsFromSnippet("""
                package doesnt.matter;
                import examples.foo.bar.A;
                
                class StaticChain {
                    String test() {
                        return A.bar(true);
                    }
                }""", false);
    }

    @Test
    public void returnOp() {
        setDescription("Method returns result of Op between method result and value");
        semanticsFromSnippet("""
                package doesnt.matter;
                import examples.GetterAndSetter;
                
                class ModuloTest {
                    int calculate(GetterAndSetter g) {
                        return g.getA() % 2;
                    }
                }""", false);
        semanticsFromSnippet("""
                package doesnt.matter;
                import examples.Auxi;
                
                class ModuloTest {
                    int calculate(Auxi g) {
                        return g.instanceCallVoid() % 2;
                    }
                }""", true);
    }

    @Test
    public void unaryOpsTest() {
        setDescription("Unary Operators");
        semanticsFromSnippet("""
                package doesnt.matter;
                import examples.Auxi;
                
                class DoubleNegation {
                    boolean test(Auxi a) {
                        return !!a.instanceCallBool();
                    }
                }""", false);
        semanticsFromSnippet("""
                package doesnt.matter;
                import examples.Auxi;
                
                class DoubleNegation {
                    boolean test(Auxi a) {
                        return !(!10);
                    }
                }""", true);
    }

    @Test
    public void fieldInitTest() {
        setDescription("Tests on field initialization");
        semanticsFromSnippet("""
                package doesnt.matter;
                
                class FieldMath {
                    int limit = 100 * 2;
                    int offset = limit - 1;
                }""", false);
        semanticsFromSnippet("""
                package doesnt.matter;
                
                class FieldMath {
                    int limit = 100 * 2;
                    int offset = limit - true;
                }""", true);
    }

    @Test
    public void superCallTest() {
        setDescription("Tests on a super call");
        semanticsFromSnippet("""
                package doesnt.matter;
                import examples.inheritance.B;
                
                class SuperAccess {
                    int test() {
                        B obj;
                        obj = new B();
                        return obj.a(); // B has method a() by inheritance
                    }
                }""", false);
        semanticsFromSnippet("""
                package doesnt.matter;
                import examples.inheritance.B;
                
                class SuperAccess {
                    int test() {
                        B obj;
                        obj = new B();
                        return obj.nonExistent();
                    }
                }""", true);
    }
}
