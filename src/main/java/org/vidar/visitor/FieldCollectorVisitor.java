package org.vidar.visitor;

import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.ArrayList;
import java.util.List;

public class FieldCollectorVisitor extends VoidVisitorAdapter<Void> {


    private List<VariableDeclarator> vars;
    public FieldCollectorVisitor() {
        vars = new ArrayList<>();
    }

    @Override
    public void visit(VariableDeclarator vd, Void arg) {
        vars.add(vd);
        super.visit(vd, arg);
    }

    public List<VariableDeclarator> getVars() {
        return vars;
    }

}
