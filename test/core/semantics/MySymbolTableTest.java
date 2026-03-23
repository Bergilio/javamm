// java
package core.semantics;

import org.junit.Test;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.ast.Kind;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.test.env.JmmTestEnv;
import pt.up.fe.comp2026.jmm.ast.JmmKind;

import static org.junit.Assert.assertFalse;
import static pt.up.fe.comp.cp1.core.parser.RulesNames.*;

public class MySymbolTableTest extends JmmTestEnv {
    private static final String BASE_PATH = "core/semantics/symboltable/";
    private static final String RESOURCES_LOCATION = "test";

    public MySymbolTableTest() {
        super(BASE_PATH, RESOURCES_LOCATION);
    }

    @Test
    public void testFirstExample() {
        var st = symbolTable("Factorial.jmm", false).getSymbolTable();
        assertEquals("Expected class name to be Factorial", "Factorial", st.getClassName());
        var mainMethodsList = st.getMethods("main");
        assertTrue("Expected to encounter main method", !mainMethodsList.isEmpty());
    }

    @Test
    public void duplicateLocalVarDifferentType() {
        var result = symbolTable("DuplicateLocalVarDifferentType.jmm", true);
        assertFalse("Expected errors for duplicate local variable with different type", result.getReports(ReportType.ERROR).isEmpty());
    }

    @Test
    public void duplicateParamDifferentType() {
        var result = symbolTable("DuplicateParamDifferentType.jmm", true);
        assertFalse("Expected errors for duplicate parameters with different type", result.getReports(ReportType.ERROR).isEmpty());
    }

    @Test
    public void duplicateParamAndLocalVarDifferentType() {
        var result = symbolTable("DuplicateParamAndLocalVarDifferentType.jmm", true);
        assertFalse("Expected errors for duplicate parameter and local variable with different type", result.getReports(ReportType.ERROR).isEmpty());
    }
}
