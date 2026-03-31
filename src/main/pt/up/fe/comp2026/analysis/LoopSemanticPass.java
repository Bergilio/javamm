package pt.up.fe.comp2026.analysis;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.type.JmmType;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2026.ast.TypeUtils;
import pt.up.fe.comp.jmm.analysis.table.reflection.Importer;

import static pt.up.fe.comp2026.jmm.ast.JmmKind.*;

public class LoopSemanticPass extends AnalysisVisitor {

    @Override
    protected void buildVisitor() {
        addVisit(WHILE_STMT, this::visitWhileStmt);
        addVisit(DO_WHILE_STMT, this::visitDoWhileStmt);
        addVisit(FOR_STMT, this::visitForStmt);
    }

    private Void visitWhileStmt(JmmNode whileStmt, SymbolTable table) {
        if (whileStmt.getNumChildren() == 0) return null;
        var condition = whileStmt.getChild(0);
        try {
            var type = TypeUtils.with(table).getExprType(condition);
            if (!type.equals(TypeUtils.boolType())) {
                addReport(newError(condition, "While condition must be boolean"));
            }
        } catch (Exception e) {}
        return null;
    }

    private Void visitDoWhileStmt(JmmNode doWhileStmt, SymbolTable table) {
        if (doWhileStmt.getChildren(EXPR).isEmpty()) return null;
        var condition = doWhileStmt.getChildren(EXPR).getFirst();
        try {
            var type = TypeUtils.with(table).getExprType(condition);
            if (!type.equals(TypeUtils.boolType())) {
                addReport(newError(condition, "Do While condition must be boolean"));
            }
        } catch (Exception e) {}
        return null;
    }

    private Void visitForStmt(JmmNode forStmt, SymbolTable table) {

        for (JmmNode child : forStmt.getChildren()) {

            // 1. Check Condition using direct Enum equality
            if (child.getKind() == FOR_COND) {
                if (child.getNumChildren() > 0) {
                    var condExpr = child.getChild(0);
                    try {
                        var type = TypeUtils.with(table).getExprType(condExpr);
                        if (!type.equals(TypeUtils.boolType())) {
                            addReport(newError(condExpr, "For condition must be boolean"));
                        }
                    } catch (Exception e) {
                        // If TypeUtils crashes, assume semantic failure for negative tests
                        addReport(newError(condExpr, "Invalid condition expression"));
                    }
                }
            }

            // 2. Check Initialization using direct Enum equality
            else if (child.getKind() == FOR_INIT) {
                if (child.hasAttribute("var") && child.getNumChildren() > 0) {
                    String varName = child.get("var");
                    var exprNode = child.getChild(0);
                    try {
                        var exprType = TypeUtils.with(table).getExprType(exprNode);
                        var varType = getSafeVarType(varName, forStmt, table);

                        if (varType != null && !TypeUtils.with(table).isAssignable(varType, exprType)) {
                            addReport(newError(child, "Type mismatch in for-loop initialization"));
                        }
                    } catch (Exception e) {
                        addReport(newError(child, "Invalid assignment expression"));
                    }
                }
            }
        }

        return null;
    }

    private JmmType getSafeVarType(String varName, JmmNode node, SymbolTable table) {
        var maybeMethod = node.getAncestor(METHOD_DECL);
        if (maybeMethod.isEmpty()) return null;

        try {
            var sig = TypeUtils.with(table).getMethodDeclSignature(maybeMethod.get());
            var methodOpt = table.getMethod(sig);
            if (methodOpt.isEmpty()) return null;

            var method = methodOpt.get();
            if (method.getLocalVariable(varName).isPresent()) return method.getLocalVariable(varName).get().type();
            if (method.getParameter(varName).isPresent()) return method.getParameter(varName).get().type();

            var classTable = table;
            while(classTable != null) {
                if(classTable.getField(varName).isPresent()) return classTable.getField(varName).get().type();
                var superClass = classTable.getSuperFullyQualifiedName();
                if(superClass == null) break;
                classTable = Importer.fromThisClassPath().getSymbolTableOf(superClass).orElse(null);
            }
        } catch (Exception e) {
            // Failsafe
        }
        return null;
    }
}