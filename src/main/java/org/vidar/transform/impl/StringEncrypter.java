package org.vidar.transform.impl;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithStatements;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.SwitchEntry;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.vidar.entity.StringEntry;
import org.vidar.transform.Transformer;
import org.vidar.utils.EncryptorUtil;
import org.vidar.utils.StatementUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StringEncrypter implements Transformer<Void> {
    @Override
    public void transform(CompilationUnit cu, Void arg) {
        cu.accept(new StringEncryptVisitor(),null);
    }

    private static class StringEncryptVisitor extends VoidVisitorAdapter<Void> {
        @Override
        public void visit(ClassOrInterfaceDeclaration clz, Void arg) {

            List<MethodDeclaration> methods = clz.findAll(MethodDeclaration.class);
            for (MethodDeclaration method : methods) {
                if (!method.getBody().isPresent()) {
                    return;
                }
                run(method.getBody().get());
            }

            for (ConstructorDeclaration constructor : clz.findAll(ConstructorDeclaration.class)) {
                run(constructor.getBody());
            }
            for (InitializerDeclaration initializer : clz.findAll(InitializerDeclaration.class)) {
                run(initializer.getBody());
            }

        }

        private void run(BlockStmt body) {
            // 提取字符串并替换为变量 Extract string to variable
            Map<Statement, List<StringEntry>> encStrings = extractAndReplace(body);

            // 插入解密例程 Insert decryption routine
            for (Map.Entry<Statement, List<StringEntry>> entry : encStrings.entrySet()) {
                Statement stmt = entry.getKey();
                List<StringEntry> strEntries = entry.getValue();
                for (StringEntry strEntry : strEntries) {
                    NodeList<Statement> decStmts = EncryptorUtil.makeDecryptor(strEntry);
                    NodeWithStatements<?> parent = (NodeWithStatements<?>) stmt.getParentNode().get();
                    parent.getStatements().addAll(parent.getStatements().indexOf(stmt), decStmts);
                }
            }
        }

        /**
         * 提取字符串字面量并替换为变量。
         * @param block 要从中提取和替换字符串的代码块语句。
         * @return
         */
        private Map<Statement, List<StringEntry>> extractAndReplace(BlockStmt block) {
            Map<Statement, List<StringEntry>> result = new HashMap<>();

            ModifierVisitor<Void> visitor = new ModifierVisitor<Void>() {
                @Override
                public Visitable visit(StringLiteralExpr n, Void arg) {
                    if (n.getValue().isEmpty()) {
                        return super.visit(n, arg);
                    }
                    StringEntry entry = new StringEntry(n.asString());
                    Statement topStmt = StatementUtil.findParentBlock(n);

                    result.computeIfAbsent(topStmt, k -> new ArrayList<>()).add(entry);
                    return new NameExpr(entry.getVarName());
                }

                @Override
                public Visitable visit(SwitchEntry n, Void arg) { // Skip string constants from `switch`
                    NodeList<Statement> statements = modifyList(n.getStatements(), arg);
                    n.setStatements(statements);
                    return n;
                }

                private <N extends Node> NodeList<N> modifyList(NodeList<N> list, Void arg) {
                    return (NodeList<N>) list.accept(this, arg);
                }
            };
            block.accept(visitor, null);
            return result;
        }
    }
}
