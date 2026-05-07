package pt.up.fe.comp2026.optimization;

import org.specs.comp.ollir.OllirParseResult;
import org.specs.comp.ollir.OllirParser;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ollir.JmmOptimization;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.ollir.OllirUtils;
import pt.up.fe.comp2026.CompilerConfig;


import java.util.Collections;

public class JmmOptimizationImpl implements JmmOptimization {

    @Override
    public OllirResult toOllir(JmmSemanticsResult semanticsResult) {

        // Create visitor that will generate the OLLIR code
        var visitor = new OllirGeneratorVisitor(semanticsResult.getSymbolTable());

        // Visit the AST and obtain OLLIR code
        var ollirCode = visitor.visit(semanticsResult.getRootNode());

//        System.out.println("\nOLLIR:\n\n" + ollirCode);

        return new OllirResult(semanticsResult, ollirCode, Collections.emptyList());
    }

    @Override
    public JmmSemanticsResult transformAst(JmmSemanticsResult semanticsResult) {
        // To add the flag for the more advanced optimizations
        var transformer = new AstTransformationVisitor(semanticsResult.getSymbolTable());
        transformer.visit(semanticsResult.getRootNode());

        if(CompilerConfig.getOptimize(semanticsResult.config())){
            while(true) {
                var constProp = new AstConstantPropagationVisitor(semanticsResult.getSymbolTable());
                var constFold = new AstConstantFoldingVisitor();
                var branchElimination = new AstBranchElimination();
                constFold.visit(semanticsResult.getRootNode());
                constProp.visit(semanticsResult.getRootNode());
                branchElimination.visit(semanticsResult.getRootNode());
                if(!constFold.madeChanges() && !constProp.madeChanges() && !branchElimination.madeChanges())
                    break;
            }
        }
        return semanticsResult;
    }

    @Override
    public OllirResult transformOllir(OllirResult ollirResult) {
        if (ollirResult.config().getOrDefault("debug", "false").equals("true")) {
            System.out.println("OLLIR CODE:");
            System.out.println(ollirResult.getOllirCode());
        }

        if(CompilerConfig.getOptimize(ollirResult.config())){
            var classUnit = ollirResult.getOllirClass();
            for(var method : classUnit.getMethods()){
                if(method.isConstructMethod())
                    continue;

                var deadCode = new OllirDeadCodeElimination(method);
                boolean changed;

                do{
                    method.buildCFG();
                    method.buildVarTable();
                    changed = deadCode.simplify();
                }
                while (changed);
            }
        }

        int n = CompilerConfig.getRegisterAllocation(ollirResult.config());
        if (n == -1) return ollirResult; // default: no register allocation

        var classUnit = ollirResult.getOllirClass();
        classUnit.buildCFGs();
        classUnit.buildVarTables();

        for (var method : classUnit.getMethods()) {
            if (method.isConstructMethod()) continue;
            var allocator = new RegisterAllocator(method, n);
            allocator.allocate();
        }

        return ollirResult;
    }
}
