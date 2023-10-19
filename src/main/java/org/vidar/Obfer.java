package org.vidar;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.vidar.transform.Transformer;
import org.vidar.transform.impl.*;
import org.vidar.utils.FileUtil;
import org.vidar.utils.NameUtil;
import org.vidar.utils.TransformUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

/**
 * 启动器
 */
public class Obfer {
    private static Transformer clzRenamer = null;
    private static Transformer clzRefRenamer = null;
    private static MethodRenamer methodRenamer = null;
    private static Transformer methodRefRenamer = null;
    private static Transformer fieldRenamer = null;

    private static Transformer stringEncrypter = null;

    static {
        clzRenamer = new ClzRenamer();
        clzRefRenamer = new ClzRefRenamer();
        methodRenamer = new MethodRenamer();
        methodRefRenamer = new MethodRefRenamer();
        fieldRenamer = new FieldRenamer();
        stringEncrypter = new StringEncrypter();
    }

    public static void main(String[] args) throws IOException {
        printLogoAndUsage();
        // 解析启动参数
        OptionParser parser = new OptionParser();
        parser.accepts("path").withRequiredArg().required();
//        parser.accepts("path").withOptionalArg();
        OptionSet options = parser.parse(args);

        String sourcePath = options.has("path") ? (String) options.valueOf("path") : "/Users/zhchen/Downloads/chatin/XpocGUI/src/main/java";

        // 预处理，遍历目录中的所有Java文件
        Files.walk(Paths.get(sourcePath)).filter(p -> p.toString().endsWith(".java")).forEach(p -> {
            FileInputStream fis = FileUtil.getFileInputStream(p);
            CompilationUnit cu = TransformUtil.getCompilationUnit(fis);

            // 1.1 混淆类名
            String oldClzName = TransformUtil.getClzNameByPath(p);
            String oldPkgName = cu.getPackageDeclaration().orElse(new PackageDeclaration()).getNameAsString();
            // 指定唯一包名，生成全局唯一className,TODO 改善，暂时不修改包名
            String newClzName = NameUtil.generateClassName("1ue");
            clzRenamer.transform(cu, newClzName);

            // 3 混淆字段名
            fieldRenamer.transform(cu,newClzName);

            // 保存文件
            String path = p.toFile().getParent();
            FileUtil.saveModifiedFile(cu, new File(path + "/" + newClzName + ".java"));
            FileUtil.deleteFile(p.toFile());

            // 1.2 混淆类名后，需修改引用类的地方
            changeClzUsage(sourcePath, oldPkgName, oldPkgName, oldClzName, newClzName);

        });
        // 2.1 混淆方法名
//            methodRenamer.transform(cu, newClzName);
        changeMethodName(sourcePath);
        // 2.2 混淆方法后，需修改用到方法的地方
        changeMethodUsage(sourcePath,methodRenamer.getMethodNameMap());

        System.out.println("begin obfucate strings...");

        // 混淆字符串
        // TODO 注解中字符混淆
        Files.walk(Paths.get(sourcePath)).filter(p -> p.toString().endsWith(".java")).forEach(p -> {
            FileInputStream fis = FileUtil.getFileInputStream(p);
            CompilationUnit cu = TransformUtil.getCompilationUnit(fis);
            stringEncrypter.transform(cu,null);
            FileUtil.saveModifiedFile(cu,p.toFile());
        });

        System.out.println("finished!!!");

    }

    private static void changeMethodName(String sourcePath) {
        try {
            // 遍历目录中的所有Java文件
            Files.walk(Paths.get(sourcePath)).filter(p -> p.toString().endsWith(".java")).forEach(p -> {

                FileInputStream fis = FileUtil.getFileInputStream(p);
                CompilationUnit cu = TransformUtil.getCompilationUnit(fis);
                // 修改引用的方法
                methodRenamer.transform(cu,null);
                // 保存修改后的源代码
                FileUtil.saveModifiedFile(cu, p.toFile());
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void changeClzUsage(String sourcePath, String oldPkgName, String newPkgName, String oldClzName, String newClzName) {
        try {
            Files.walk(Paths.get(sourcePath)).filter(p -> p.toString().endsWith(".java")).forEach(p -> {
                FileInputStream fis = FileUtil.getFileInputStream(p);
                CompilationUnit cu = TransformUtil.getCompilationUnit(fis);
                clzRefRenamer.transform(cu, new String[]{oldClzName, newClzName});

                // 修改旧的import为新的import
                TransformUtil.changeImport(cu, oldPkgName + "." + oldClzName, newPkgName + "." + newClzName);
                // 保存修改后的源代码
                FileUtil.saveModifiedFile(cu, p.toFile());
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void changeMethodUsage(String sourcePath, Map<String, String> needChange) {
        System.out.println("the method need to Change:");
        needChange.keySet().forEach(System.out::println);
        System.out.println("=========================");
        try {
            // 遍历目录中的所有Java文件
            Files.walk(Paths.get(sourcePath)).filter(p -> p.toString().endsWith(".java")).forEach(p -> {

                FileInputStream fis = FileUtil.getFileInputStream(p);
                CompilationUnit cu = TransformUtil.getCompilationUnit(fis);
                // 修改引用的方法
                methodRefRenamer.transform(cu,needChange);

                // 保存修改后的源代码
                FileUtil.saveModifiedFile(cu, p.toFile());
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void printLogoAndUsage() {
        System.out.println(" ╦╔═╗┌─┐┬ ┬┬─┐┌─┐┌─┐  ╔═╗┌┐ ┌─┐\n" +
                " ║╚═╗│ ││ │├┬┘│  ├┤───║ ║├┴┐├┤ \n" +
                "╚╝╚═╝└─┘└─┘┴└─└─┘└─┘  ╚═╝└─┘└  ");
        System.out.println();
        System.out.println("usage: --path 指定混淆的java目录");
        System.out.println();
    }
}
