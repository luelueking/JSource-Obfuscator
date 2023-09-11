package org.vidar.visitor;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

/**
 * 修改Java文件中的类引用
 * arg[0]:待替换类名
 * arg[1]:替换类名
 */
public class ClzRefChangeVisitor extends VoidVisitorAdapter<String[]> {

    @Override
    public void visit(ClassOrInterfaceType parent, String[] arg) {
        // 修改类
        if (parent.getNameAsString().equals(arg[0])) {
            parent.setName(arg[1]);
        }
        super.visit(parent,arg);
    }
}