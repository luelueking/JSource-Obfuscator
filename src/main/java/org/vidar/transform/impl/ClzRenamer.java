package org.vidar.transform.impl;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.vidar.transform.Transformer;

public class ClzRenamer implements Transformer<String> {
    @Override
    public void transform(CompilationUnit cu, String newClzName) {
        cu.accept(new VoidVisitorAdapter<String>() {
            @Override
            public void visit(ClassOrInterfaceDeclaration cd, String newClzName) {
                cd.setName(newClzName);
                cd.getConstructors().forEach(constructor -> constructor.setName(newClzName));
                super.visit(cd, newClzName);
            }
        },newClzName);
    }

}
