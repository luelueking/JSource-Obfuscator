package org.vidar.utils;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

public class TransformUtil {


    private static JavaParser javaParser = null;

    static {
        CombinedTypeSolver typeSolver = new CombinedTypeSolver();
        typeSolver.add(new ReflectionTypeSolver());
        ParserConfiguration parserConfiguration = new ParserConfiguration();
        parserConfiguration.setSymbolResolver(new JavaSymbolSolver(typeSolver));
        javaParser = new JavaParser(parserConfiguration);
    }


    public static void changeImport(CompilationUnit cu, String oldImportName, String newImportName) {
        AtomicBoolean hasOldImport = new AtomicBoolean(false);
        Collection<ImportDeclaration> toDelete = new ArrayList<>();
        cu.getImports().forEach(importDeclaration -> {
            if (importDeclaration.getNameAsString().contains(oldImportName)) {
                toDelete.add(importDeclaration);
                hasOldImport.set(true);
            }
        });
        cu.getImports().removeAll(toDelete);
        if (hasOldImport.get()) {
            cu.addImport(newImportName);
        }
    }

    public static String getClzNameByFile(File file) {
        String filename = file.getName();
        int extensionIndex = filename.lastIndexOf(".");
        return extensionIndex == -1 ? filename : filename.substring(0, extensionIndex);
    }

    public static String getClzNameByPath(Path p) {
       return getClzNameByFile(p.toFile());
    }

    public static String getMethodSignature(MethodDeclaration m) {
        String methodName = m.getNameAsString();
        String returnType = m.getType().toString();
        String parameters = m.getParameters().toString();
        return methodName + "/" + returnType + "/" + parameters;
    }

    public static CompilationUnit getCompilationUnit(FileInputStream fis) {
        CompilationUnit cu = javaParser.parse(fis).getResult().get();
        try {
            fis.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return cu;
    }
}
