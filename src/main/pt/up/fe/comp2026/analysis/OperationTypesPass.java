package pt.up.fe.comp2026.analysis;

import pt.up.fe.comp.jmm.analysis.table.MethodSymbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.type.JmmType;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2026.ast.TypeUtils;
import pt.up.fe.comp2026.jmm.ast.JmmAttributes;
import pt.up.fe.comp2026.jmm.ast.JmmKind;

public class OperationTypesPass extends AnalysisVisitor {
    private MethodSymbol currentMethod;
    @Override
    public void buildVisitor() {
        addVisit(JmmKind.BINARY_EXPR, this::visitBinaryExpr);
        addVisit(JmmKind.UNARY_EXPR, this::visitUnaryExpr);
    }

    private Void visitBinaryExpr(JmmNode expr, SymbolTable table) {
        var left = TypeUtils.with(table).getExprType(expr.getChild(0));
        var right = TypeUtils.with(table).getExprType(expr.getChild(1));
        var op = expr.get("op");
        JmmType op_operands = null;
        switch (op) {
            case "+", "*", "/", "-", "%", "<", ">", "<=", ">=":
                op_operands = TypeUtils.intType();
                break;
            case "&&", "||":
                op_operands = TypeUtils.boolType();
                break;
            case "==", "!=": //These should accept anything, right?
                if(left != right)
                    addReport(newError(expr, " operands for binary expression"));
                return null;
        }
        if(!left.equals(right) || !left.equals(op_operands) || !right.equals(op_operands))
            addReport(newError(expr, "Invalid operands for binary expression"));
        return null;
    }

    private Void visitUnaryExpr(JmmNode expr, SymbolTable table) {
        var operand = TypeUtils.with(table).getExprType(expr.getChild(0));
        var op = TypeUtils.with(table).getExprType(expr);
        if(!op.equals(operand))
            addReport(newError(expr, "Invalid operand for unary expression"));
        return null;
    }
}
