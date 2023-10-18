package org.vidar.transform.impl;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import org.vidar.transform.Transformer;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

// TODO 如果是不同类相同函数名，但是其中一个是接口不会被修改，导致错误被改，即现在不允许出现重复函数名
public class MethodRefRenamer implements Transformer<Map<String, String>> {

    @Override
    public void transform(CompilationUnit cu, Map<String, String> arg) {
        cu.accept(new MethodRefChangeVisitor(),arg);
        cu.accept(new StaticMethodCallVisitor(),arg);
    }

    private static class MethodRefChangeVisitor extends VoidVisitorAdapter<Map<String, String>> {

        @Override
        public void visit(MethodCallExpr mc, Map<String, String> needChange) {
            String className = null;


            AtomicBoolean isInterface = new AtomicBoolean(false);
            try {
                ResolvedMethodDeclaration resolve = mc.resolve();
                className = resolve.getClassName();
            } catch (Exception e) {
                // ignore 这里必须忽略异常，你细品
            }


            String methodName = mc.getNameAsString();
            if ( className == null || className.isEmpty() || "null".equals(className) ) {
                if (mc.getScope().isPresent() && mc.getScope().get() instanceof NameExpr) {
                    NameExpr scopeExpr = (NameExpr) mc.getScope().get();
                    className = scopeExpr.getNameAsString();
                }
            }
//            String sign = className + "#" + methodName;
            System.out.println("-------------");
//            System.out.println(sign);
            needChange.keySet().forEach(System.out::println);
            System.out.println("-------------");
            if (needChange.containsKey(methodName)) {
                mc.setName(needChange.get(methodName));
            }
            super.visit(mc, needChange);
        }

    }

    private static class StaticMethodCallVisitor extends VoidVisitorAdapter<Map<String, String>> {
        @Override
        public void visit(MethodCallExpr methodCallExpr, Map<String, String> needChange) {
//            if (methodCallExpr.getScope().isPresent() && methodCallExpr.getScope().get() instanceof NameExpr) {
//                NameExpr scopeExpr = (NameExpr) methodCallExpr.getScope().get();
//                String className = scopeExpr.getNameAsString();
//                String methodName = methodCallExpr.getNameAsString();
//                System.out.println("------------");
//                System.out.println(methodName);
//                needChange.keySet().forEach(System.out::println);
//                if (needChange.containsKey(className+"#"+methodName)) {
//                    methodCallExpr.setName(needChange.get(methodName));
//                }
//                System.out.println("------------");
//            }
            super.visit(methodCallExpr,needChange);
        }
    }
}


