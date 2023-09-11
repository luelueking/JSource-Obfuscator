import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ClassNameChange {
    public static void main(String[] args) throws FileNotFoundException {
        // 读取要修改的Java文件
        File fileToModify = new File("/Users/zhchen/Downloads/obf-test/src/main/java/org/example/bean/Egg.java");

        String newClzName = "Eog";
        // 解析Java文件为抽象语法树
        CompilationUnit cu = StaticJavaParser.parse(fileToModify);
        String clzName = cu.getPrimaryTypeName().orElse("Unknown");
        System.out.println(clzName);
        // 获取原始类名
        String originalClassName = cu.getClassByName(clzName).orElseThrow(() -> new IllegalStateException("Class not found")).getNameAsString();
        // 修改类名
        cu.accept(new ClzNameChangeVisitor(), newClzName);
        // 保存修改后的源代码
        saveModifiedFile(cu, new File("/Users/zhchen/Downloads/obf-test/src/main/java/org/example/bean/Eog.java"));
    }

    private static class ClzNameChangeVisitor extends VoidVisitorAdapter<String> {

        @Override
        public void visit(ClassOrInterfaceDeclaration cd, String newClzName) {
            // 修改类名
            cd.setName(newClzName);
            cd.getConstructors().forEach(constructor -> {
                constructor.setName(newClzName);
            });
            super.visit(cd, newClzName);
        }
    }
    private static void saveModifiedFile(CompilationUnit cu, File file) {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(cu.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
