package org.vidar.visitor;

import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.Optional;

public class FieldNameChangeVisitor extends VoidVisitorAdapter<String[]> {
    @Override
    public void visit(VariableDeclarator vd, String[] arg) {
        if (vd.getNameAsString().equals(arg[0])) {
            vd.setName(arg[1]);
        }
        super.visit(vd, arg);
    }

    @Override
    public void visit(FieldAccessExpr fe, String[] arg) {
//        System.out.println(fe.getName().getIdentifier());
        if (fe.getName().getIdentifier().equals(arg[0])) {
            fe.setName(arg[1]);
        }
        super.visit(fe,arg);
    }

    @Override
    public void visit(NameExpr nameExpr, String[] arg) {
//        System.out.println(nameExpr.getName().getIdentifier());
        if (nameExpr.getNameAsString().equals(arg[0])) {
            nameExpr.setName(arg[1]);
        }
        super.visit(nameExpr,arg);
    }
}
