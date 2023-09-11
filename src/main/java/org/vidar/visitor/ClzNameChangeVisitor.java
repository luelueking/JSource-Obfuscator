package org.vidar.visitor;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

/**
 * 修改Java文件中的主类名
 */
public class ClzNameChangeVisitor extends VoidVisitorAdapter<String> {
    @Override
    public void visit(ClassOrInterfaceDeclaration cd, String newClzName) {
        // 修改类名
        cd.setName(newClzName);
        cd.getConstructors().forEach(constructor -> {
            constructor.setName(newClzName);
        });
        super.visit(cd, newClzName);
    }
}