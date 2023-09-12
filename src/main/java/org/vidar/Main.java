package org.vidar;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.resolution.SymbolResolver;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import org.vidar.utils.FileUtil;
import org.vidar.utils.NameUtils;
import org.vidar.utils.TransformUtil;
import org.vidar.visitor.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {


    private static JavaParser javaParser = null;

    static {

        CombinedTypeSolver typeSolver = new CombinedTypeSolver();
        typeSolver.add(new ReflectionTypeSolver());
        ParserConfiguration parserConfiguration = new ParserConfiguration();
        parserConfiguration.setSymbolResolver(new JavaSymbolSolver(typeSolver));
        javaParser = new JavaParser(parserConfiguration);
    }

    public static void main(String[] args) {
        try {
            // 遍历目录中的所有Java文件
            Files.walk(Paths.get("/Users/zhchen/Downloads/obf-test/src/main/java/")).filter(p -> p.toString().endsWith(".java")).forEach(p -> {
                try {
                    // 读取Java文件
                    File file = p.toFile();
                    FileInputStream fis = new FileInputStream(file);
//                    CompilationUnit cu = StaticJavaParser.parse(fis);
                    CompilationUnit cu = javaParser.parse(fis).getResult().get();
                    // 原来的类名
                    String oldClzName = TransformUtil.getClzNameByFile(file);
                    String oldPkgName = cu.getPackageDeclaration().orElse(new PackageDeclaration()).getNameAsString();
                    // 擦去类名
                    // 指定唯一包名，生成全局唯一className,TODO 改善
                    String newClzName = NameUtils.generateClassName("1ue");

                    // 混淆类名
                    cu.accept(new ClzNameChangeVisitor(), newClzName);

                    // 混淆方法名
                    MethodCollectorVisitor methodCollectorVisitor = new MethodCollectorVisitor();
                    cu.accept(methodCollectorVisitor,null);
                    // 1.收集所有方法名
                    List<MethodDeclaration> methods = methodCollectorVisitor.getMethods();
                    HashMap<String, String> methodNameMap = new HashMap<>();
                    for (MethodDeclaration m : methods) {
                        String oldMethodName = m.getNameAsString();
                        // 新的方法名
                        String newMethodName = NameUtils.generateMethodName(newClzName, oldMethodName);
                        methodNameMap.put(newClzName+"#"+oldMethodName,newMethodName);
                        cu.accept(new MethodNameChangeVisitor(),new String[]{oldMethodName,newMethodName});
                    }

                    String path = file.getParent();
                    FileUtil.saveModifiedFile(cu, new File( path + "/" + newClzName + ".java"));
                    FileUtil.deleteFile(file);
                    changeUsage(oldPkgName,oldPkgName,oldClzName,newClzName);
                    changeMethodUsage(methodNameMap);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static void changeUsage(String oldPkgName, String newPkgName, String oldClzName, String newClzName) {
        try {
            // 遍历目录中的所有Java文件
            Files.walk(Paths.get("/Users/zhchen/Downloads/obf-test/src/main/java/")).filter(p -> p.toString().endsWith(".java")).forEach(p -> {
                try {
                    // 读取Java文件
                    File file = p.toFile();
                    FileInputStream fis = new FileInputStream(file);
//                    CompilationUnit cu = StaticJavaParser.parse(fis);
                    CompilationUnit cu = javaParser.parse(fis).getResult().get();
                    // 修改引用的类名
                    cu.accept(new ClzRefChangeVisitor(), new String[] { oldClzName, newClzName });
                    String oldImportName = oldPkgName + "." + oldClzName;
                    cu.addImport(newPkgName + "." + newClzName);
                    TransformUtil.deleteImport(cu, oldImportName);
                    // 保存修改后的源代码
                    FileUtil.saveModifiedFile(cu, file);
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // todo
    private static void changeMethodUsage(Map<String,String> needChange) {
        try {
            // 遍历目录中的所有Java文件
            Files.walk(Paths.get("/Users/zhchen/Downloads/obf-test/src/main/java/")).filter(p -> p.toString().endsWith(".java")).forEach(p -> {
                try {
                    // 读取Java文件
                    File file = p.toFile();
                    FileInputStream fis = new FileInputStream(file);
//                    CompilationUnit cu = StaticJavaParser.parse(fis);
                    CompilationUnit cu = javaParser.parse(fis).getResult().get();
                    // 修改引用的方法
                    cu.accept(new MethodRefChangeVisitor(),needChange);
                    // 保存修改后的源代码
                    FileUtil.saveModifiedFile(cu, file);
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
