/*
 * Avaj
 * Copyright (C) 2022 Cg <cg@bytecodeking.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.vidar.utils;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.Statement;
import org.vidar.entry.Constant;
import org.vidar.entry.Round;
import org.vidar.entry.StringEntry;
import org.vidar.entry.operators.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Encryptor {
    private static final List<AbstractOperator> OPERATORS = new ArrayList<AbstractOperator>() {{
        add(new AddOperator());
        add(new SubOperator());
        add(new XorOperator());
        add(new NotOperator());
        add(new NegOperator());
        add(new AddIndexOperator());
        add(new SubIndexOperator());
        add(new XorIndexOperator());
        add(new ShiftOperator());
    }};

    private static final List<AbstractOperator> FALLBACK_OPERATORS = new ArrayList<AbstractOperator>() {{
        add(new XorOperator());
    }};

    public static NodeList<Statement> makeDecryptor(StringEntry entry) {
        NodeList<Statement> resultStmts = new NodeList<>();
        SimpleName dataVar = new SimpleName(NameUtils.generateLocalVariableName());
        SimpleName indexVar = new SimpleName(NameUtils.generateLocalVariableName());
        SimpleName roundValueVar = new SimpleName(NameUtils.generateLocalVariableName());

        // Create variable for result string
        resultStmts.add(new ExpressionStmt(new VariableDeclarationExpr(new VariableDeclarator(
                StaticJavaParser.parseType("String"),
                entry.getVarName().asString(),
                new StringLiteralExpr("")))));

        if (entry.getRawString().isEmpty()) {
            return resultStmts;
        }

        // Create random rounds (1 ~ 5)
        int roundCount = ThreadLocalRandom.current().nextInt(5) + 1;
        List<AbstractOperator> roundOperators = new ArrayList<>();
        double strength = 0;
        for (int i = 0; i < roundCount; i++) {
            AbstractOperator op = OPERATORS.get(ThreadLocalRandom.current().nextInt(OPERATORS.size()));
            roundOperators.add(op);
            strength += op.getStrength();
        }
        if (strength < 1.0D) {
            roundOperators.add(FALLBACK_OPERATORS.get(ThreadLocalRandom.current().nextInt(FALLBACK_OPERATORS.size())));
        }
        Collections.shuffle(roundOperators);

        // Extra constants as opaque predicate
        Constant idxCst = new Constant(indexVar);

        // Make rounds
        List<Round> rounds = new ArrayList<>();
        for (AbstractOperator op : roundOperators) {
            rounds.add(op.makeRound(roundValueVar, idxCst));
        }

        // Encrypte
        char[] rawChars = entry.getRawString().toCharArray();
        int[] encResult = new int[rawChars.length];
        for (int i = 0; i < rawChars.length; i++) {
            int cur = rawChars[i];
            for (Round round : rounds) {
                cur = round.getResult(cur, i);
            }
            encResult[i] = cur;
        }

        // Create encrypted data array
        ArrayInitializerExpr arrayExpr = new ArrayInitializerExpr();
        for (int element : encResult) {
            arrayExpr.getValues().add(new IntegerLiteralExpr(String.format("0x%04X", element)));
        }
        resultStmts.add(new ExpressionStmt(new VariableDeclarationExpr(new VariableDeclarator(
                StaticJavaParser.parseType("int[]"),
                dataVar,
                arrayExpr
        ))));

        // Create decryption routine
        ForStmt forStmt = new ForStmt();
        BlockStmt routineBody = new BlockStmt();
        forStmt.setBody(routineBody);

        VariableDeclarator indexVarDec = new VariableDeclarator(StaticJavaParser.parseType("int"), indexVar);
        VariableDeclarator roundValueVarDec = new VariableDeclarator(StaticJavaParser.parseType("int"), roundValueVar);
        VariableDeclarationExpr varDecExpr = new VariableDeclarationExpr();
        indexVarDec.setInitializer(new IntegerLiteralExpr("0"));
        roundValueVarDec.setInitializer(new IntegerLiteralExpr("0"));
        varDecExpr.getVariables().add(indexVarDec);
        varDecExpr.getVariables().add(roundValueVarDec);
        forStmt.getInitialization().add(varDecExpr);
        forStmt.setCompare(new BinaryExpr(
                new NameExpr(indexVar),
                new IntegerLiteralExpr(String.valueOf(encResult.length)),
                BinaryExpr.Operator.LESS));
        forStmt.getUpdate().add(new UnaryExpr(new NameExpr(indexVar), UnaryExpr.Operator.POSTFIX_INCREMENT));
        resultStmts.add(forStmt);

        // Expr: roundValueVar = dataVar[indexVar]
        routineBody.addAndGetStatement(new ExpressionStmt(
                new AssignExpr(new NameExpr(roundValueVar),
                        new ArrayAccessExpr(new NameExpr(dataVar), new NameExpr(indexVar)),
                        AssignExpr.Operator.ASSIGN)));

        Collections.reverse(rounds);
        for (Round round : rounds) {
            routineBody.addAndGetStatement(round.getStatement());
        }

        // Expr: stringEntry += (char) (roundValueVar & 0xFFFF)
        routineBody.addAndGetStatement(new ExpressionStmt(
                new AssignExpr(new NameExpr(entry.getVarName()),
                        new CastExpr(StaticJavaParser.parseType("char"),
                                new EnclosedExpr(new BinaryExpr(new NameExpr(roundValueVar),
                                        new IntegerLiteralExpr("0xFFFF"),
                                        BinaryExpr.Operator.BINARY_AND))),
                        AssignExpr.Operator.PLUS)));

        return resultStmts;
    }
}
