package org.vidar.transform.impl;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.vidar.transform.Transformer;
import org.vidar.utils.NameUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MethodRenamer implements Transformer<Void> {
    HashMap<String, String> methodNameMap = new HashMap<>();

    @Override
    public void transform(CompilationUnit cu,Void arg) {
        MethodCollectorVisitor methodCollectorVisitor = new MethodCollectorVisitor();
        cu.accept(methodCollectorVisitor,null);

        List<MethodDeclaration> methods = methodCollectorVisitor.getMethods();

        for (MethodDeclaration m : methods) {
            String oldMethodName = m.getNameAsString();
            // 新的方法名
            String newMethodName = NameUtil.generateMethodName("newClzName", oldMethodName);
//            methodNameMap.put(newClzName + "#" + oldMethodName, newMethodName);
            methodNameMap.put(oldMethodName, newMethodName);
            cu.accept(new MethodNameChangeVisitor(), new String[]{oldMethodName, newMethodName});
        }

    }


    public HashMap<String, String> getMethodNameMap() {
        return methodNameMap;
    }


    private static class MethodCollectorVisitor extends VoidVisitorAdapter<Void> {
        private List<MethodDeclaration> methods;

        public MethodCollectorVisitor() {
            methods = new ArrayList<>();
        }

        @Override
        public void visit(MethodDeclaration md, Void arg) {
            super.visit(md, arg);
            boolean isInterface = false;
            // 获取方法所在的父节点
            TypeDeclaration<?> parent = md.findAncestor(TypeDeclaration.class).orElse(null);
            if (parent != null && parent.isClassOrInterfaceDeclaration()) {
                ClassOrInterfaceDeclaration clz = (ClassOrInterfaceDeclaration) parent;
                isInterface = clz.isInterface();
            }
            boolean hasOverrideAnnotation = md.getAnnotations().stream()
                    .anyMatch(annotation -> annotation.getNameAsString().equals("Override"));
//            System.out.println(isInterface);
            if(!hasOverrideAnnotation && !isInterface && !md.getModifiers().contains(Modifier.staticModifier())) {
               // 将方法添加到列表中
                methods.add(md);
            }
        }

        public List<MethodDeclaration> getMethods() {
            return methods;
        }
    }

    private static class MethodNameChangeVisitor extends VoidVisitorAdapter<String[]> {

        @Override
        public void visit(ConstructorDeclaration cd, String[] arg) {
            for (Parameter parameter : cd.getParameters()) {
                String oldName = parameter.getName().getIdentifier();
                String newName = NameUtil.generateLocalVariableName();
                // 修改参数名称
                parameter.setName(newName);

                // 遍历方法内的语句
                for (Statement statement : cd.getBody().getStatements()) {
                    statement.findAll(NameExpr.class).forEach(nameExpr -> {
                        if (nameExpr.getNameAsString().equals(oldName)) {
                            // 修改使用到参数的地方的名称
                            nameExpr.setName(newName);
                        }
                    });
                }
            }
        }

        @Override
        public void visit(MethodDeclaration md, String[] arg) {

            /* 修改方法参数 */
            // 获取方法的参数列表
            for (Parameter parameter : md.getParameters()) {
                String oldName = parameter.getName().getIdentifier();
                String newName = NameUtil.generateLocalVariableName();
                // 修改参数名称
                parameter.setName(newName);

                // 遍历方法内的语句
                md.getBody().ifPresent(body -> {
                    for (Statement statement : body.getStatements()) {
                        // 查找使用到参数的地方
                        statement.findAll(NameExpr.class).forEach(nameExpr -> {
                            if (nameExpr.getNameAsString().equals(oldName)) {
                                // 修改使用到参数的地方的名称
                                nameExpr.setName(newName);
                            }
                        });
                    }
                });
            }



            /* 修改方法名 */
            // 检查注解列表中是否包含 @Override 注解
            boolean hasOverrideAnnotation = md.getAnnotations().stream()
                    .anyMatch(annotation -> annotation.getNameAsString().equals("Override"));


            boolean isInterface = false;
            // 获取方法所在的父节点
            TypeDeclaration<?> parent = md.findAncestor(TypeDeclaration.class).orElse(null);
            if (parent != null && parent.isClassOrInterfaceDeclaration()) {
                ClassOrInterfaceDeclaration clz = (ClassOrInterfaceDeclaration) parent;
                isInterface = clz.isInterface();
            }

            boolean isMain = md.getNameAsString().equals("main");

            if (!md.isNative() && !hasOverrideAnnotation && !isInterface && !isMain) {
                if (md.getNameAsString().equals(arg[0])) {
                    md.setName(arg[1]);
                }
            }
            super.visit(md, arg);
        }

        @Override
        public void visit(MethodCallExpr mc, String[] arg) {
            if (mc.getNameAsString().equals(arg[0])) {
                mc.setName(arg[1]);
            }

            // 递归处理scope中的方法调用
            while (mc.getScope().isPresent() && mc.getScope().get() instanceof MethodCallExpr) {
                for (Expression argument : mc.getArguments()) {
                    if (argument instanceof MethodCallExpr) {
                        MethodCallExpr argumentmc = (MethodCallExpr) argument;
                        if (argumentmc.getNameAsString().equals(arg[0])) {
                            argumentmc.setName(arg[1]);
                        }
//                    System.out.println(argumentmc.getName());
                        while (argumentmc.getScope().isPresent() && argumentmc.getScope().get() instanceof MethodCallExpr) {
                            argumentmc = (MethodCallExpr) argumentmc.getScope().get();
                            if (argumentmc.getNameAsString().equals(arg[0])) {
                                argumentmc.setName(arg[1]);
                            }
                        }
                    }
                }
                mc = (MethodCallExpr) mc.getScope().get();
                if (mc.getNameAsString().equals(arg[0])) {
                    mc.setName(arg[1]);
                }
            }
            super.visit(mc, arg);
        }

    }
}


