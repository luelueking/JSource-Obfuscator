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

package org.vidar.entry.operators;

import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SimpleName;

import java.util.concurrent.ThreadLocalRandom;

public class SubOperator extends AbstractOperator {
    @Override
    protected BinaryExpr.Operator getSimpleOperator() {
        return BinaryExpr.Operator.PLUS;
    }

    @Override
    public double getStrength() {
        return 1.0D;
    }

    @Override
    public int doRound(int value, int... constants) {
        return value - constants[0];
    }

    @Override
    protected Expression generateExpr(SimpleName variable, Expression... constants) {
        Expression left;
        Expression right;
        if (ThreadLocalRandom.current().nextBoolean()) {
            left = new NameExpr(variable);
            right = constants[0];
        } else {
            left = constants[0];
            right = new NameExpr(variable);
        }
        return new BinaryExpr(left, right, BinaryExpr.Operator.PLUS);
    }
}
