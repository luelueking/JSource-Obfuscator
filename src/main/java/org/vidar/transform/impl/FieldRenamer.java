package org.vidar.transform.impl;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.vidar.transform.Transformer;
import org.vidar.utils.NameUtil;

import java.util.ArrayList;
import java.util.List;

public class FieldRenamer implements Transformer<String> {
    @Override
    public void transform(CompilationUnit cu, String newClzName) {
        FieldCollectorVisitor fieldCollectorVisitor = new FieldCollectorVisitor();
        ParameterVisitor parameterVisitor = new ParameterVisitor();

        cu.accept(parameterVisitor, null);
        cu.accept(fieldCollectorVisitor, null);

        List<VariableDeclarator> vars = fieldCollectorVisitor.getVars();
        List<Parameter> parameters = parameterVisitor.getParameters();

        for (VariableDeclarator var : vars) {
            boolean flag = false;
            for (Parameter p : parameters) {
                if (var.getNameAsString().equals(p.getNameAsString())) {
                    flag = true;
                }
            }
            if (flag) {
                continue;
            }
            String oldVarName = var.getNameAsString();
            String newVarName = NameUtil.generateFieldName(newClzName);
            cu.accept(new FieldNameChangeVisitor(), new String[]{oldVarName, newVarName});
        }
    }

    private static class ParameterVisitor extends VoidVisitorAdapter<Void> {
        private final List<Parameter> parameters;

        public ParameterVisitor() {
            parameters = new ArrayList<>();
        }

        @Override
        public void visit(Parameter n, Void arg) {
            parameters.add(n);
            super.visit(n, arg);
        }

        public List<Parameter> getParameters() {
            return this.parameters;
        }
    }

    private static class FieldCollectorVisitor extends VoidVisitorAdapter<Void> {
        private final List<VariableDeclarator> vars;

        public FieldCollectorVisitor() {
            vars = new ArrayList<>();
        }

        @Override
        public void visit(VariableDeclarator vd, Void arg) {
            vars.add(vd);
            super.visit(vd, arg);
        }

        public List<VariableDeclarator> getVars() {
            return vars;
        }
    }

    private static class FieldNameChangeVisitor extends VoidVisitorAdapter<String[]> {
        @Override
        public void visit(Parameter n, String[] arg) {
            super.visit(n, arg);
        }

        @Override
        public void visit(VariableDeclarator vd, String[] arg) {
            if (vd.getNameAsString().equals(arg[0])) {
                vd.setName(arg[1]);
            }
            super.visit(vd, arg);
        }

        @Override
        public void visit(FieldAccessExpr fe, String[] arg) {
            if (fe.getName().getIdentifier().equals(arg[0])) {
                fe.setName(arg[1]);
            }
            super.visit(fe, arg);
        }

        @Override
        public void visit(NameExpr nameExpr, String[] arg) {
            if (nameExpr.getNameAsString().equals(arg[0])) {
                nameExpr.setName(arg[1]);
            }
            super.visit(nameExpr, arg);
        }
    }
}
