package org.vidar.transform.impl;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.vidar.transform.Transformer;

public class ClzRefRenamer implements Transformer<String[]> {
    @Override
    public void transform(CompilationUnit cu, String[] arg) {
        cu.accept(new VoidVisitorAdapter<String[]>() {
            @Override
            public void visit(ClassOrInterfaceType parent, String[] arg) {
                if (parent.getNameAsString().equals(arg[0])) {
                    parent.setName(arg[1]);
                }
                super.visit(parent,arg);
            }
        },arg);
        cu.accept(new ClassNameRenamingVisitor(),arg);
    }
}

class ClassNameRenamingVisitor extends VoidVisitorAdapter<String[]> {
    @Override
    public void visit(MethodCallExpr methodCallExpr, String[] arg) {
        if (methodCallExpr.getScope().isPresent() && methodCallExpr.getScope().get() instanceof NameExpr) {
            NameExpr scopeExpr = (NameExpr) methodCallExpr.getScope().get();
            String className = scopeExpr.getNameAsString();
            if (className.equals(arg[0])) {
                scopeExpr.setName(arg[1]);
            }
        }
        super.visit(methodCallExpr,arg);
    }
}
