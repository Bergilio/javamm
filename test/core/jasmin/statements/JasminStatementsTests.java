package core.jasmin.statements;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import pt.up.fe.comp.cp3.BaseJasminTestEnv;

@RunWith(Parameterized.class)
public class JasminStatementsTests extends BaseJasminTestEnv {
    private static final String BASE_PATH = "core/jasmin/statements/";

    public JasminStatementsTests(InputSource inputSource) {
        super(inputSource, BASE_PATH);
    }

    @Test
    public void StatementsTest() {
        var res = toJasmin("JasminStatements");
        var ret = res.invoke("method", Boolean.class);
        assertTrue("Return should be of type true", ret.returnValue());
    }
}