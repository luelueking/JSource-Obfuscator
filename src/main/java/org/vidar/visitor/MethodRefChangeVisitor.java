package org.vidar.visitor;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;

import java.util.Map;

public class MethodRefChangeVisitor extends VoidVisitorAdapter<Map<String, String>> {

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
