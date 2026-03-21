package pt.up.fe.comp2026.analysis;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2026.ast.TypeUtils;

import java.util.ArrayList;
import java.util.List;

import static pt.up.fe.comp2026.jmm.ast.JmmKind.IF_STMT;

/**
 * Implementation of AnalysisPass that automatically visits nodes using preorder traversal.
 */
public abstract class AnalysisVisitor extends PreorderJmmVisitor<SymbolTable, Void> implements AnalysisPass {

    private List<Report> reports;

    public AnalysisVisitor() {
        reports = new ArrayList<>();
        setDefaultValue(() -> null);
    }

    protected void addReport(Report report) {
        reports.add(report);
    }

    protected List<Report> getReports() {
        return reports;
    }


    @Override
    public List<Report> analyze(JmmNode root, SymbolTable table) {
        // Visit the node
        visit(root, table);

        // Return reports
        return getReports();
    }

    public Report newError(JmmNode node, String message) {
        return Report.newError(
                Stage.SEMANTIC,
                node.getLine(),
                node.getColumn(),
                message,
                null);
    }

    public static class IfElseSemanticPass extends AnalysisVisitor {

        @Override
        protected void buildVisitor() {
            addVisit(IF_STMT, this::visitIfStmt);
        }

        private Void visitIfStmt(JmmNode ifStmt, SymbolTable table) {
            var condition = ifStmt.getChild(0);

            var type = TypeUtils.with(table).getExprType(condition);

            if (!type.equals(TypeUtils.boolType())) {
                addReport(newError(condition, "If condition must be boolean"));
            }
            return null;
        }
    }
}
