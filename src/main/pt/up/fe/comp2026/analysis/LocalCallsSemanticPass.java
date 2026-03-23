package pt.up.fe.comp2026.analysis;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;

import static pt.up.fe.comp2026.jmm.ast.JmmKind.*;

public class LocalCallsSemanticPass extends AnalysisVisitor {

    @Override
    protected void buildVisitor() {
        addVisit(IMPLICIT_THIS_FUNCTION_CALL, this::visitImplicitThisCall);
        addVisit(FUNCTION_CALL, this::visitFunctionCall);
    }

    private Void visitImplicitThisCall(JmmNode call, SymbolTable table) {
        var methodName = call.get("func");
        var methods = table.getMethods(methodName);
        if (methods.isEmpty() && table.getSuper() == null) {
            addReport(newError(call, "Method '" + methodName + "' does not exist"));
        }
        return null;
    }

    private Void visitFunctionCall(JmmNode functionCall, SymbolTable table) {
        var methodName = functionCall.get("func");
        var target = functionCall.getChild(0);

        boolean isLocalCall = target.isInstance(THIS) ||
                (target.isInstance(VAR_REF_EXPR) && target.get("name").equals("this"));

        if (!isLocalCall) {
            return null;
        }

        var methods = table.getMethods(methodName);
        if (methods.isEmpty() && table.getSuper() == null) {
            addReport(newError(functionCall, "Method '" + methodName + "' does not exist"));
        }
        return null;
    }
}