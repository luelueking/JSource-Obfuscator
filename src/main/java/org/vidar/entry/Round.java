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

package org.vidar.entry;

import com.github.javaparser.ast.stmt.Statement;
import org.apache.commons.lang3.ArrayUtils;
import org.vidar.entry.operators.AbstractOperator;

public class Round {
    private final AbstractOperator operator;
    private final Statement statement;
    private final int[] constants;

    public Round(AbstractOperator operator, Statement statement, int[] constants) {
        this.operator = operator;
        this.statement = statement;
        this.constants = constants;
    }

    public AbstractOperator getOperator() {
        return operator;
    }

    public Statement getStatement() {
        return statement;
    }

    public int[] getConstants() {
        return constants;
    }

    public int getResult(int value, int... csts) {
        return operator.doRound(value, ArrayUtils.addAll(constants, csts));
    }
}
