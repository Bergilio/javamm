package pt.up.fe.comp2026.analysis;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.type.JmmType;
import pt.up.fe.comp.jmm.analysis.table.type.impls.JmmArrayType;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2026.ast.TypeUtils;
import pt.up.fe.comp2026.jmm.ast.JmmAttributes;
import pt.up.fe.comp2026.jmm.ast.JmmKind;

import javax.swing.text.AbstractDocument;

import static pt.up.fe.comp2026.jmm.ast.JmmKind.EXPR;

public class ArrayPass extends AnalysisVisitor{
    @Override
    public void buildVisitor() {
        addVisit(JmmKind.ARRAY_INDEX, this::visitArrayIndex);
        addVisit(JmmKind.LENGTH_OPERATOR, this::visitLength);
        addVisit(JmmKind.ARRAY_ALLOC_INIT, this::visitArrayInit);
        addVisit(JmmKind.ARRAY_ALLOC, this::visitArrayAlloc);
    }

    private Void visitArrayIndex(JmmNode arrayInd, SymbolTable table) {
        var ind = arrayInd.getChild(0);
        if(!TypeUtils.with(table).getExprType(ind).equals(TypeUtils.intType()))
            addReport(newError(arrayInd, "The index does not evaluate to an integer."));
        return null;
    }

    private Void visitLength(JmmNode lengthOp, SymbolTable table) {
        var measured = lengthOp.getChild(0);
        if(!(TypeUtils.with(table).getExprType(measured) instanceof  JmmArrayType))
            addReport(newError(lengthOp, "The length can only be applied to an array."));
        return null;
    }

    private Void visitArrayInit(JmmNode arrayInit, SymbolTable table) {
        var elements = arrayInit.getChildren(EXPR);
        var arrayType = TypeUtils.with(table).getExprType(arrayInit);
        JmmType base_type;
        if(arrayType.asArray().dimension() == 1)
            base_type = arrayType.asArray().itemType();
        else
            base_type = new JmmArrayType(arrayType.asArray().itemType(), arrayType.asArray().dimension());

        if(elements.stream().map(e -> TypeUtils.with(table).getExprType(e)).filter(e -> TypeUtils.with(table).isAssignable(base_type, e)).count() != elements.size())
            addReport(newError(arrayInit, "Elements attributed to the array do not match the declared type."));
        return null;
    }

    private Void visitArrayAlloc(JmmNode arrayAlloc, SymbolTable table) {
        if(!(JmmKind.check(arrayAlloc.getChild(0), JmmKind.ARRAY_INDEX)))   //Forces the first dimension to have a defined size, particularly for multidim arrays
            addReport(newError(arrayAlloc, "The first dimension of an array must be defined."));
        var missing = false;
        var inits = arrayAlloc.getChildren();
        for(var i : inits)
            if(i.getChildren().isEmpty())
                missing = true;
            else if(missing) {
                addReport(newError(arrayAlloc, "Gap in array dimension definition."));
                return null;
            }
        
        return null;
    }
}
