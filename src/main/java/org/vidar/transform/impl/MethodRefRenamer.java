package org.vidar.transform.impl;

import com.github.javaparser.Range;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.MethodReferenceExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import org.vidar.transform.Transformer;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

// TODO 如果是不同类相同函数名，但是其中一个是接口不会被修改，导致错误被改，即现在不允许出现重复函数名
public class MethodRefRenamer implements Transformer<Map<String, String>> {

    private Map<String,String> vartoClz = new HashMap<>();

    @Override
    public void transform(CompilationUnit cu, Map<String, String> arg) {
        cu.accept(new VarClzCollectVisitor(),vartoClz);
//        System.out.println("var mapping ClzName");
//        vartoClz.forEach((key, value) -> System.out.println(key + " : " + value));
        cu.accept(new MethodRefChangeVisitor(),new Map[]{arg,vartoClz});
    }

    // 获取var对应的ClassName
    private static class VarClzCollectVisitor extends VoidVisitorAdapter<Map> {
        @Override
        public void visit(VariableDeclarator declarator, Map vartoClz) {
            super.visit(declarator, vartoClz);
            vartoClz.put(declarator.getNameAsString(),declarator.getType().asString());
        }
    }

    private static class MethodRefChangeVisitor extends VoidVisitorAdapter<Map[]> {

        @Override
        public void visit(MethodCallExpr mc, Map[] map) {
            Map<String,String> needChange = map[0];
            Map<String,String> vartoClz = map[1];
            String className = null;


            AtomicBoolean isInterface = new AtomicBoolean(false);
            try {
                ResolvedMethodDeclaration resolve = mc.resolve();
                className = resolve.getClassName();
            } catch (Exception e) {
//                e.printStackTrace();
                // ignore 这里必须忽略异常，你细品
            }


            String methodName = mc.getNameAsString();
            if ( className == null || className.isEmpty() || "null".equals(className) ) {

                if (mc.getScope().isPresent() && mc.getScope().get() instanceof NameExpr) {
                    NameExpr scopeExpr = mc.getScope().get().asNameExpr();
                    String varName = scopeExpr.getName().getIdentifier();
                    className = vartoClz.get(varName);
                }
            }
            String sign = className + "#" + methodName;

            if (needChange.containsKey(sign)) {
                String s = needChange.get(sign);
//                System.out.println(sign+"------->"+s);
                mc.setName(s);
            }
            super.visit(mc, map);
        }


    }

}


