package pt.up.fe.comp2026.optimization;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PostorderJmmVisitor;
import pt.up.fe.comp2026.jmm.ast.JmmKind;

public class AstBranchElimination extends PostorderJmmVisitor<Void, Void> {
    private boolean changed = false;

    @Override
    protected void buildVisitor() {
        addVisit(JmmKind.IF_STMT, this::visitIfStmt);
        addVisit(JmmKind.WHILE_STMT, this::visitWhileStmt);
        setDefaultVisit(this::defaultVisit);
    }

    private Void defaultVisit(JmmNode node, Void unused) {
        return null;
    }

    private Void visitIfStmt(JmmNode node, Void unused) {
        var expr = node.getChild(0);
        if(expr.isInstance(JmmKind.BOOLEAN_LITERAL)) {
            JmmNode validBranch;
            if(expr.get("value").equals("true"))
                validBranch = node.getChild(1);
            else if(node.getChildren().size() == 3)
                validBranch = node.getChild(2);
            else
                validBranch = null;

            if(validBranch != null) {
                validBranch.removeParent();
                node.replace(validBranch);
            }
            else
                node.removeParent();
            changed = true;
        }
        return null;
    }

    private Void visitWhileStmt(JmmNode node, Void unused) {
        var expr = node.getChild(0);
        if(expr.isInstance(JmmKind.BOOLEAN_LITERAL))
            if(expr.get("value").equals("false")) {
                node.removeParent();
                changed = true;
            }

        return null;
    }

    public boolean madeChanges() {
        return changed;
    }
}
