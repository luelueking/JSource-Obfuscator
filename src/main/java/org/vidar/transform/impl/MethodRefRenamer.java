package org.vidar.transform.impl;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import org.vidar.transform.Transformer;

import java.util.HashMap;
import java.util.Map;

public class MethodRefRenamer implements Transformer<Map<String, String>> {
    private final Map<String,String> varToClz = new HashMap<>();
    @Override
    @SuppressWarnings("unchecked")
    public void transform(CompilationUnit cu, Map<String, String> arg) {
        cu.accept(new VarClzCollectVisitor(), varToClz);
        cu.accept(new MethodRefChangeVisitor(),new Map[]{arg, varToClz});
    }
    private static class VarClzCollectVisitor extends VoidVisitorAdapter<Map<String,String>> {
        @Override
        public void visit(VariableDeclarator declarator, Map<String,String> varToClz) {
            super.visit(declarator, varToClz);
            varToClz.put(declarator.getNameAsString(),declarator.getType().asString());
        }
    }
    private static class MethodRefChangeVisitor extends VoidVisitorAdapter<Map<String,String>[]> {
        @Override
        public void visit(MethodCallExpr mc, Map<String,String>[] map) {
            Map<String,String> needChange = map[0];
            Map<String,String> varToClz = map[1];
            String className = null;
            try {
                ResolvedMethodDeclaration resolve = mc.resolve();
                className = resolve.getClassName();
            } catch (Exception ignored) {
            }
            String methodName = mc.getNameAsString();
            if ( className == null || className.isEmpty() || "null".equals(className) ) {
                if (mc.getScope().isPresent() && mc.getScope().get() instanceof NameExpr) {
                    NameExpr scopeExpr = mc.getScope().get().asNameExpr();
                    String varName = scopeExpr.getName().getIdentifier();
                    className = varToClz.get(varName);
                }
                if(mc.getScope().isPresent() && mc.getScope().get() instanceof ObjectCreationExpr) {
                    ObjectCreationExpr objectCreationExpr = mc.getScope().get().asObjectCreationExpr();
                    className = objectCreationExpr.getTypeAsString();
                }
            }
            String sign = className + "#" + methodName;
            if (needChange.containsKey(sign)) {
                String s = needChange.get(sign);
                mc.setName(s);
            }
            super.visit(mc, map);
        }
    }
}


