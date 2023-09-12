import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import org.vidar.utils.NameUtils;
import org.vidar.visitor.MethodNameChangeVisitor;

import java.io.File;
import java.io.FileNotFoundException;

import static org.vidar.utils.FileUtil.saveModifiedFile;

public class MethodNameChange {
    public static void main(String[] args) throws FileNotFoundException {
        // 读取要修改的Java文件
        File fileToModify = new File("/Users/zhchen/Downloads/obf-test/src/main/java/org/example/config/SecurityConfig.java");

        // 解析Java文件为抽象语法树
        CompilationUnit cu = StaticJavaParser.parse(fileToModify);
        String clzName = cu.getPrimaryTypeName().orElse("Unknown");
        System.out.println(clzName);
        // 获取原始类名
        String originalClassName = cu.getClassByName(clzName).orElseThrow(() -> new IllegalStateException("Class not found")).getNameAsString();
        // 修改类名
        cu.accept(new MethodNameChangeVisitor(), new String[]{"passwordEncoder","hhh"});
        // 保存修改后的源代码
        saveModifiedFile(cu, new File("/Users/zhchen/Downloads/obf-test/src/main/java/org/example/config/SecurityConfig-ovf.java"));
    }
}
