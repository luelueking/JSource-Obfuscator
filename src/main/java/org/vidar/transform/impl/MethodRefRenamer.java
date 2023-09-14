package org.vidar.transform.impl;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import org.vidar.transform.Transformer;

import java.util.Map;

public class MethodRefRenamer implements Transformer<Map<String, String>> {

    @Override
    public void transform(CompilationUnit cu, Map<String, String> arg) {
        cu.accept(new MethodRefChangeVisitor(),arg);
    }

    private static class MethodRefChangeVisitor extends VoidVisitorAdapter<Map<String, String>> {

        @Override
        public void visit(MethodCallExpr mc, Map<String, String> needChange) {
            String className = null;


            try {
                ResolvedMethodDeclaration resolve = mc.resolve();
                className = resolve.getClassName();
//            System.out.println(resolve.getClassName());
            } catch (Exception e) {
                // ignore 这里必须忽略异常，你细品
            }

            String methodName = mc.getNameAsString();
            String sign = className + "#" + methodName;
            if (needChange.containsKey(methodName)) {
                mc.setName(needChange.get(methodName));
            }
            super.visit(mc, needChange);
        }

    }
}


