package org.vidar.visitor;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.ArrayList;
import java.util.List;

public class MethodCollectorVisitor extends VoidVisitorAdapter<Void> {
        private List<MethodDeclaration> methods;

        public MethodCollectorVisitor() {
            methods = new ArrayList<>();
        }

        @Override
        public void visit(MethodDeclaration md, Void arg) {
            super.visit(md, arg);
            // 将方法添加到列表中
            methods.add(md);
        }

        public List<MethodDeclaration> getMethods() {
            return methods;
        }
    }