package org.vidar.visitor;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.MethodReferenceExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.vidar.utils.TransformUtil;

import java.util.Map;
import java.util.Optional;

public class MethodNameChangeVisitor extends VoidVisitorAdapter<String[]> {
    @Override
    public void visit(MethodDeclaration md, String[] arg) {
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
        super.visit(mc,arg);
    }

}

