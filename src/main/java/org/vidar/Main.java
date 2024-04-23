package org.vidar;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import me.n1ar4.log.LogLevel;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;
import org.vidar.transform.Transformer;
import org.vidar.transform.impl.*;
import org.vidar.utils.FileUtil;
import org.vidar.utils.NameUtil;
import org.vidar.utils.TransformUtil;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Stream;

public class Main {
    private static final Logger logger = LogManager.getLogger();
    private static final Transformer<String> clzRenamer;
    private static final Transformer<String[]> clzRefRenamer;
    private static final MethodRenamer methodRenamer;
    private static final Transformer<Map<String, String>> methodRefRenamer;
    private static final Transformer<String> fieldRenamer;
    private static final Transformer<Void> stringEncryptor;

    static {
        clzRenamer = new ClzRenamer();
        clzRefRenamer = new ClzRefRenamer();
        methodRenamer = new MethodRenamer();
        methodRefRenamer = new MethodRefRenamer();
        fieldRenamer = new FieldRenamer();
        stringEncryptor = new StringEncrypter();
    }

    public static void main(String[] args) {
        LogManager.setLevel(LogLevel.INFO);
        printLogoAndUsage();
        OptionParser parser = new OptionParser();
        parser.accepts("path").withRequiredArg().required();
        parser.accepts("obfMethod").withOptionalArg();
        OptionSet options = parser.parse(args);
        String sourcePath = (String) options.valueOf("path");
        Path sPath = Paths.get(sourcePath);

        Stream<Path> files;
        try {
            files = Files.walk(sPath);
        } catch (Exception ex) {
            logger.error("file walk error: {}", ex.toString());
            return;
        }

        logger.info("start obfuscate class name");

        files.filter(p -> p.toString().endsWith(".java")).forEach(p -> {
            FileInputStream fis = FileUtil.getFileInputStream(p);
            CompilationUnit cu = TransformUtil.getCompilationUnit(fis);
            String oldClzName = TransformUtil.getClzNameByPath(p);
            String oldPkgName = cu.getPackageDeclaration().orElse(new PackageDeclaration()).getNameAsString();
            String newClzName = NameUtil.generateClassName();
            clzRenamer.transform(cu, newClzName);
//            fieldRenamer.transform(cu, newClzName);
            String path = p.toFile().getParent();
            FileUtil.saveModifiedFile(cu, new File(path + "/" + newClzName + ".java"));
            FileUtil.deleteFile(p.toFile());
            changeClzUsage(sPath, oldPkgName, oldPkgName, oldClzName, newClzName);
        });
        files.close();

        boolean obfMethod = !options.has("obfMethod") ||
                (boolean) options.valueOf("obfMethod");
        if (obfMethod) {
            changeMethodName(sPath);
            changeMethodUsage(sPath, methodRenamer.getMethodNameMap());
        }

        logger.info("start obfuscate strings");

        try {
            files = Files.walk(sPath);
        } catch (Exception ex) {
            logger.error("file walk error: {}", ex.toString());
            return;
        }

        files.filter(p -> p.toString().endsWith(".java")).forEach(p -> {
            FileInputStream fis = FileUtil.getFileInputStream(p);
            CompilationUnit cu = TransformUtil.getCompilationUnit(fis);
            stringEncryptor.transform(cu, null);
            FileUtil.saveModifiedFile(cu, p.toFile());
        });
        files.close();

        logger.info("finished");
    }

    private static void changeMethodName(Path sourcePath) {
        System.out.println("start obfuscate method name");
        Stream<Path> files;
        try {
            files = Files.walk(sourcePath);
        } catch (Exception ex) {
            logger.error("file walk error: {}", ex.toString());
            return;
        }
        files.filter(p -> p.toString().endsWith(".java")).forEach(p -> {
            FileInputStream fis = FileUtil.getFileInputStream(p);
            CompilationUnit cu = TransformUtil.getCompilationUnit(fis);
            methodRenamer.transform(cu, null);
            FileUtil.saveModifiedFile(cu, p.toFile());
        });
        files.close();
    }

    private static void changeClzUsage(Path sourcePath, String oldPkgName, String newPkgName, String oldClzName, String newClzName) {
        Stream<Path> files;
        try {
            files = Files.walk(sourcePath);
        } catch (Exception ex) {
            logger.error("file walk error: {}", ex.toString());
            return;
        }
        files.filter(p -> p.toString().endsWith(".java")).forEach(p -> {
            FileInputStream fis = FileUtil.getFileInputStream(p);
            CompilationUnit cu = TransformUtil.getCompilationUnit(fis);
            clzRefRenamer.transform(cu, new String[]{oldClzName, newClzName});
            TransformUtil.changeImport(cu, oldPkgName + "." + oldClzName,
                    newPkgName + "." + newClzName);
            FileUtil.saveModifiedFile(cu, p.toFile());
        });
        files.close();
    }

    private static void changeMethodUsage(Path sourcePath, Map<String, String> needChange) {
        Stream<Path> files;
        try {
            files = Files.walk(sourcePath);
        } catch (Exception ex) {
            logger.error("file walk error: {}", ex.toString());
            return;
        }
        files.filter(p -> p.toString().endsWith(".java")).forEach(p -> {
            FileInputStream fis = FileUtil.getFileInputStream(p);
            CompilationUnit cu = TransformUtil.getCompilationUnit(fis);
            methodRefRenamer.transform(cu, needChange);
            FileUtil.saveModifiedFile(cu, p.toFile());
        });
        files.close();
    }

    private static void printLogoAndUsage() {
        System.out.println(
                " ╦╔═╗┌─┐┬ ┬┬─┐┌─┐┌─┐  ╔═╗┌┐ ┌─┐\n" +
                " ║╚═╗│ ││ │├┬┘│  ├┤───║ ║├┴┐├┤ \n" +
                "╚╝╚═╝└─┘└─┘┴└─└─┘└─┘  ╚═╝└─┘└  ");
        System.out.println();
        System.out.println("usage: --path [java-source-dir] --obfMethod=true [method-name-obfuscate]");
        System.out.println();
    }
}
