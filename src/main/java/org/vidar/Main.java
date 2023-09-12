package org.vidar;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import org.vidar.utils.FileUtil;
import org.vidar.utils.NameUtils;
import org.vidar.utils.TransformUtil;
import org.vidar.visitor.ClzNameChangeVisitor;
import org.vidar.visitor.ClzRefChangeVisitor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        try {
            // 遍历目录中的所有Java文件
            Files.walk(Paths.get("/Users/zhchen/Downloads/obf-test/src/main/java/")).filter(p -> p.toString().endsWith(".java")).forEach(p -> {
                try {
                    // 读取Java文件
                    File file = p.toFile();
                    FileInputStream fis = new FileInputStream(file);
                    CompilationUnit cu = StaticJavaParser.parse(fis);
                    // 原来的类名
                    String oldClzName = TransformUtil.getClzNameByFile(file);
                    String oldPkgName = cu.getPackageDeclaration().orElse(new PackageDeclaration()).getNameAsString();
                    // 擦去类名
                    // 指定唯一包名，生成全局唯一className,TODO 改善
                    String newClzName = NameUtils.generateClassName("1ue");
                    cu.accept(new ClzNameChangeVisitor(), newClzName);
                    String path = file.getParent();
                    FileUtil.saveModifiedFile(cu, new File( path + "/" + newClzName + ".java"));
                    FileUtil.deleteFile(file);
                    changeUsage(oldPkgName,oldPkgName,oldClzName,newClzName);

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
                    CompilationUnit cu = StaticJavaParser.parse(fis);
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
}
