package core.jasmin.calls;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import pt.up.fe.comp.cp3.BaseJasminTestEnv;

@RunWith(Parameterized.class)
public class JasminCallsTests extends BaseJasminTestEnv {
    private static final String BASE_PATH = "core/jasmin/calls/";

    public JasminCallsTests(InputSource inputSource) {super(inputSource, BASE_PATH);}

    @Test
    public void LocalCallImplicitThis() {
        var res = toJasmin("JasminCall");
        var ret = res.invoke("double", Integer.class);
        var expected = 0;
        assertEquals("method should return ${expected}", expected, ret.returnValue());
    }

    @Test
    public void LocalCallStaticMethod() {
        var res = toJasmin("JasminCall");
        var ret = res.invoke("zero", Integer.class);
        var expected = 0;
        assertEquals("method should return ${expected}", expected, ret.returnValue());
    }

    @Test
    public void LocalCallOverwriteVar() {
        var res = toJasmin("JasminCall");
        var ret = res.invoke("method", Integer.class);
        var expected = 0;
        assertEquals("method should return ${expected}", expected, ret.returnValue());
    }
}
