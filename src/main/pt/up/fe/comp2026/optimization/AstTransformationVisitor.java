package pt.up.fe.comp2026.optimization;

import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.JmmNodeImpl;
import pt.up.fe.comp2026.jmm.ast.JmmKind;

import java.util.Collections;
import java.util.List;

public class AstTransformationVisitor extends AJmmVisitor<Void, Void> {
    private boolean changed = false;

    @Override
    protected void buildVisitor() {
        addVisit(JmmKind.FOR_STMT, this::visitFor);
        addVisit(JmmKind.DO_WHILE_STMT, this::visitDoWhile);
        addVisit(JmmKind.PARENTHESIS, this::visitParenthesis);
        addVisit(JmmKind.METHOD_DECL, this::visitMethodDecl);
        setDefaultVisit(this::visitDefault);
        //addVisit(JmmKind.IF_STMT, this::visitIfStmt);
        //addVisit(JmmKind.METHOD_DECL, this::visitMethodDeadCode);
        //addVisit(JmmKind.WHILE_STMT, this::visitImpossibleWhile);
    }

    private Void visitDefault(JmmNode node, Void unused) {
        return null;
    }
    //Transform a for in a while loop
    private Void visitFor(JmmNode forStmt, Void unused) {
        var assignFor = forStmt.getChildren(JmmKind.ASSIGN_FOR);
        if (!assignFor.isEmpty()) {
            var expr = assignFor.getFirst().getChild(0);
            var id = assignFor.getFirst().get("var");
            var newNode = new JmmNodeImpl(JmmKind.ASSIGN_STMT);
            newNode.put("var", id);
            newNode.add(expr);
            var index = forStmt.getIndexOfSelf();
            var parent = forStmt.getParent();
            parent.add(newNode, index);
        }

        var stmtsInScope = forStmt.getChildren(JmmKind.STMT).getFirst();
        var eval = forStmt.getChildren(JmmKind.EXPR);
        var assignFor2 = forStmt.getChildren(JmmKind.ASSIGN_FOR);
        var newNode = new JmmNodeImpl(JmmKind.WHILE_STMT);
        if(!eval.isEmpty())
            newNode.add(eval.getFirst());
        else {
            var newEval = new JmmNodeImpl(JmmKind.BOOLEAN_LITERAL);
            newEval.put("value", "true");
            newNode.add(newEval);
        }
        if(!assignFor2.isEmpty())
            stmtsInScope.add(assignFor2.getFirst());

        changed = true;
        newNode.add(stmtsInScope);
        forStmt.replace(newNode);
        return null;
    }

    //Transform a do while in a while loop
    private Void visitDoWhile(JmmNode doWhileStmt, Void unused) {
        var stmt = doWhileStmt.getChild(0);
        var expr = doWhileStmt.getChild(1);
        List<JmmNode> stmts;

        if(stmt.isInstance(JmmKind.BLOCK))
            stmts = stmt.getChildren();
        else
            stmts = Collections.singletonList(stmt);

        var parent = doWhileStmt.getParent();
        for(var st : stmts)
            parent.add(st, doWhileStmt.getIndexOfSelf());

        changed = true;
        var newNode = new JmmNodeImpl(JmmKind.WHILE_STMT);
        newNode.add(expr);
        newNode.add(stmt);
        doWhileStmt.replace(newNode);
        return null;
    }

    private Void visitParenthesis(JmmNode paren, Void unused) {
        var child = paren.getChild(0);
        child.removeParent();
        paren.replace(child);
        return null;
    }

    private Void visitMethodDecl(JmmNode methodDecl, Void unused) {
        var isRet = methodDecl.getChildren().getLast().isInstance(JmmKind.RETURN_STMT);
        if(!isRet)
            methodDecl.add(new JmmNodeImpl(JmmKind.RETURN_STMT));
        return null;
    }

    public boolean madeChanges() {
        return changed;
    }

    /*
    //BELLOW, WE WILL HAVE IFs AND DEAD CODE OPTIMIZATIONS. Not sure if we would need the "-o" flag for these.

    //Remove statically solved if stmts
    private Void visitIfStmt(JmmNode ifStmt, Void unused) {
        var expr = ifStmt.getChild(0);
        JmmNode stmt;
        List<JmmNode> stmts;

        if(expr.isInstance(JmmKind.BOOLEAN_LITERAL))
            if(expr.get("value").equals("true"))
                stmt = ifStmt.getChild(1);
            else
                if(ifStmt.getChildren().size() == 3)
                    stmt = ifStmt.getChild(2);
                else
                    stmt = null;
        else
            return  null;

        if(stmt != null && stmt.isInstance(JmmKind.BLOCK))
            stmts = stmt.getChildren();
        else if(stmt != null)
            stmts = Collections.singletonList(stmt);
        else
            stmts = Collections.emptyList();

        var parent = ifStmt.getParent();
        for(var st : stmts)
            parent.add(st, ifStmt.getIndexOfSelf());

        changed = true;
        ifStmt.delete();
        return null;
    }

    //Remove death code post return
    private Void visitMethodDeadCode(JmmNode methodDecl, Void unused) {
        var returnStmt = methodDecl.getChildren(JmmKind.RETURN_STMT);
        JmmNode ret;
        if(returnStmt.isEmpty())
            return null;
        else
            ret = returnStmt.getFirst();

        var children = methodDecl.getChildren();
        var index = ret.getIndexOfSelf();
        var siblings = ret.getParent().getChildren();
        if(siblings.size() == index + 1)
            return null;    //Nothing to delete

        for(int i = index + 1; i < children.size(); i++)
            children.get(i).delete();

        changed = true;
        return null;
    }

    private Void visitImpossibleWhile(JmmNode whileStmt, Void unused) {
        var expr = whileStmt.getChild(0);
        if(expr.isInstance(JmmKind.BOOLEAN_LITERAL) && expr.get("value").equals("false")) {
            changed = true;
            whileStmt.delete();
        }

        return null;
    }
     */
}
