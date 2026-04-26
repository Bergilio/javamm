package pt.up.fe.comp2026.optimization;

import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.JmmNodeImpl;
import pt.up.fe.comp.jmm.ast.PostorderJmmVisitor;
import pt.up.fe.comp2026.jmm.ast.JmmKind;

import static pt.up.fe.comp2026.jmm.ast.JmmKind.BOOLEAN_LITERAL;
import static pt.up.fe.comp2026.jmm.ast.JmmKind.INTEGER_LITERAL;

public class AstConstantFoldingVisitor extends PostorderJmmVisitor<Void, Void> {
    private boolean changed = false;

    @Override
    protected void buildVisitor() {
        addVisit(JmmKind.BINARY_EXPR, this::visitConstantFoldingBinExpr);
        addVisit(JmmKind.UNARY_EXPR, this::visitConstantFoldingUnaryExpr);
        setDefaultVisit(this::defaultVisit);
    }

    private Void defaultVisit(JmmNode node, Void unused) {
        return null;
    }

    private Void visitConstantFoldingBinExpr(JmmNode node, Void unused) {
        var ops = node.getChildren();
        var first = ops.getFirst();
        var last = ops.getLast();
        String result;
        JmmNode newNode;
        if(first.isInstance(INTEGER_LITERAL) && last.isInstance(INTEGER_LITERAL)) {
            var val1 = Integer.parseInt(first.get("value"));
            var val2 = Integer.parseInt(last.get("value"));
            result = switch (node.get("op")) {
                case "+" -> {
                    newNode = new JmmNodeImpl(INTEGER_LITERAL);
                    yield Integer.toString(val1 + val2);
                }
                case "*" -> {
                    newNode = new JmmNodeImpl(INTEGER_LITERAL);
                    yield Integer.toString(val1 * val2);
                }
                case "/" -> {
                    newNode = new JmmNodeImpl(INTEGER_LITERAL);
                    yield Integer.toString(val1 / val2);
                }
                case "-" -> {
                    newNode = new JmmNodeImpl(INTEGER_LITERAL);
                    yield Integer.toString(val1 - val2);
                }
                case "%" -> {
                    newNode = new JmmNodeImpl(INTEGER_LITERAL);
                    yield Integer.toString(val1 % val2);
                }
                case "==" -> {
                    newNode = new JmmNodeImpl(BOOLEAN_LITERAL);
                    yield Boolean.toString(val1 == val2);
                }
                case "!=" -> {
                    newNode = new JmmNodeImpl(BOOLEAN_LITERAL);
                    yield Boolean.toString(val1 != val2);
                }
                case "<" -> {
                    newNode = new JmmNodeImpl(BOOLEAN_LITERAL);
                    yield Boolean.toString(val1 < val2);
                }
                case ">" -> {
                    newNode = new JmmNodeImpl(BOOLEAN_LITERAL);
                    yield Boolean.toString(val1 > val2);
                }
                case "<=" -> {
                    newNode = new JmmNodeImpl(BOOLEAN_LITERAL);
                    yield Boolean.toString(val1 <= val2);
                }
                case ">=" -> {
                    newNode = new JmmNodeImpl(BOOLEAN_LITERAL);
                    yield Boolean.toString(val1 >= val2);
                }
                default -> throw new RuntimeException("Unknown operator '" + node.get("op") + "'");
            };
        }
        else if(first.isInstance(BOOLEAN_LITERAL) && last.isInstance(BOOLEAN_LITERAL)) {
            var val1 = Boolean.parseBoolean(first.get("value"));
            var val2 = Boolean.parseBoolean(last.get("value"));
            newNode = new JmmNodeImpl(BOOLEAN_LITERAL);
            result = switch (node.get("op")) {
                case "==" -> Boolean.toString(val1 == val2);
                case "!=" -> Boolean.toString(val1 != val2);
                case "&&" -> Boolean.toString(val1 && val2);
                case "||" -> Boolean.toString(val1 || val2);
                default -> throw new RuntimeException("Unknown operator '" + node.get("op") + "'");
            };
        }
        else
            return null;

        changed = true;
        newNode.put("value", result);
        node.replace(newNode);
        return null;
    }

    private Void visitConstantFoldingUnaryExpr(JmmNode node, Void unused) {
        var child =  node.getChild(0);
        var op = node.get("op");
        JmmNode newNode;
        String result;
        if(child.isInstance(INTEGER_LITERAL)) {
            newNode = new JmmNodeImpl(INTEGER_LITERAL);
            if(op.equals("+") || op.equals("-"))
                result = op + child.get("value");
            else
                return null;
        }
        else if(child.isInstance(BOOLEAN_LITERAL)) {
            newNode = new JmmNodeImpl(BOOLEAN_LITERAL);
            if(op.equals("!"))
                result = Boolean.toString(!Boolean.parseBoolean(child.get("value")));
            else
                throw new RuntimeException("Unknown operator '" + op + "'");
        }
        else {
            return null;
        }

        changed = true;
        newNode.put("value", result);
        node.replace(newNode);
        return null;
    }

    public boolean madeChanges() {
        return changed;
    }
}
