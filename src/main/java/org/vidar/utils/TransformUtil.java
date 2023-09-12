package org.vidar.utils;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

public class TransformUtil {

    public static void deleteImport(CompilationUnit cu, String importName) {
        Collection<ImportDeclaration> toDelete = new ArrayList<>();
        cu.getImports().forEach(importDeclaration -> {
            if (importDeclaration.getNameAsString().contains(importName)) {
                toDelete.add(importDeclaration);
            }
        });
        cu.getImports().removeAll(toDelete);
    }

    public static String getClzNameByFile(File file) {
        String filename = file.getName();
        int extensionIndex = filename.lastIndexOf(".");
        return extensionIndex == -1 ? filename : filename.substring(0, extensionIndex);
    }

    public static String getMethodSignature(MethodDeclaration m) {
        String methodName = m.getNameAsString();
        String returnType = m.getType().toString();
        String parameters = m.getParameters().toString();
        return methodName + "/" + returnType + "/" + parameters;
    }
}
