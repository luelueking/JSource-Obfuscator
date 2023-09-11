package org.vidar.utils;

import com.github.javaparser.ast.CompilationUnit;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtil {
    public static void saveModifiedFile(CompilationUnit cu, File file) {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(cu.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void deleteFile(File file) {
        file.delete();
    }
}
