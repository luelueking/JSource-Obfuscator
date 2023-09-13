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

import com.github.javaparser.ast.expr.*;

import java.util.concurrent.ThreadLocalRandom;

public class ShiftOperator extends AbstractOperator {
    @Override
    public double getStrength() {
        return 1.0D;
    }

    @Override
    protected int[] getRandomValues() {
        int bits1 = ThreadLocalRandom.current().nextInt(31) + 1;
        int bits2 = 32 - bits1;
        return new int[]{bits1, bits2};
    }

    @Override
    public int doRound(int value, int... constants) {
        return value >>> constants[0] | value << constants[1];
    }

    @Override
    protected Expression generateExpr(SimpleName variable, Expression... constants) {
        EnclosedExpr leftShift = new EnclosedExpr(new BinaryExpr(new NameExpr(variable),
                constants[0],
                BinaryExpr.Operator.LEFT_SHIFT));
        EnclosedExpr rightShift = new EnclosedExpr(new BinaryExpr(new NameExpr(variable),
                constants[1],
                BinaryExpr.Operator.UNSIGNED_RIGHT_SHIFT));

        if (ThreadLocalRandom.current().nextBoolean()) {
            return new EnclosedExpr(new BinaryExpr(leftShift, rightShift, BinaryExpr.Operator.BINARY_OR));
        } else {
            return new EnclosedExpr(new BinaryExpr(rightShift, leftShift, BinaryExpr.Operator.BINARY_OR));
        }
    }
}
