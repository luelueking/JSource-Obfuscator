package org.vidar.utils;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.printer.DefaultPrettyPrinter;
import com.github.javaparser.printer.Printer;
import com.github.javaparser.printer.configuration.DefaultConfigurationOption;
import com.github.javaparser.printer.configuration.DefaultPrinterConfiguration;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class FileUtil {

    private static Printer printer = new DefaultPrettyPrinter();

    static {
        printer.getConfiguration().removeOption(new DefaultConfigurationOption(DefaultPrinterConfiguration.ConfigOption.PRINT_COMMENTS));
    }

    public static void saveModifiedFile(CompilationUnit cu, File file) {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            String print = printer.print(cu);
//            System.out.println("====================================================");
//            System.out.println(print);
            fos.write(print.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static FileInputStream getFileInputStream(Path path) {
        File file = path.toFile();
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        return fis;
    }

    public static void deleteFile(File file) {
        file.delete();
    }
}
