package core.jasmin.entitiesandops;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import pt.up.fe.comp.cp3.BaseJasminTestEnv;

@RunWith(Parameterized.class)
public class JasminEntitiesAndOpsTests extends BaseJasminTestEnv {
    private static final String BASE_PATH = "core/jasmin/entitiesandops/";

    public JasminEntitiesAndOpsTests(InputSource inputSource) {
        super(inputSource, BASE_PATH);
    }

    @Test
    public void EntitiesAndOpsTest() {
        var res = toJasmin("JasminEntitiesAndOps");
        var ret = res.invoke("method", Boolean.class, -1);
        assertTrue("Return should be of type ${expected}", ret.returnValue());
        ret = res.invoke("method", Boolean.class, 0);
        assertTrue("Return should be of type ${expected}", !ret.returnValue());
    }
}