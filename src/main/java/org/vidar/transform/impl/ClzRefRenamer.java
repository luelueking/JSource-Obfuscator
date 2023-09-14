package org.vidar.transform.impl;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.vidar.transform.Transformer;

public class ClzRefRenamer implements Transformer<String[]> {
    @Override
    public void transform(CompilationUnit cu, String[] arg) {
        cu.accept(new VoidVisitorAdapter<String[]>() {
            @Override
            public void visit(ClassOrInterfaceType parent, String[] arg) {
                // 修改类
                if (parent.getNameAsString().equals(arg[0])) {
                    parent.setName(arg[1]);
                }
                super.visit(parent,arg);
            }
        },arg);
    }
}
