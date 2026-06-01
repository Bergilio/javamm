package core.jasmin.declarations;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import pt.up.fe.comp.cp3.BaseJasminTestEnv;

@RunWith(Parameterized.class)
public class JasminDeclarationsTests extends BaseJasminTestEnv {
    private static final String BASE_PATH = "core/jasmin/declarations/";

    public JasminDeclarationsTests(InputSource inputSource) {
        super(inputSource, BASE_PATH);
    }

    @Test
    public void DeclarationTest() {
        var res = toJasmin("JasminDeclaration");
        var ret = res.invoke("foo", Integer.class);
        var expected = 2;
        assertEquals("Return should be of type ${expected}", expected, ret.returnValue());
    }
}