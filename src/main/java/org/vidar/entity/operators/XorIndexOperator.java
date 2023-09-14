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

package org.vidar.entity.operators;

import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SimpleName;
import org.vidar.entity.Constant;
import org.vidar.entity.Round;

public class XorIndexOperator extends XorOperator {
    @Override
    public double getStrength() {
        return 0.1D;
    }

    @Override
    public Round makeRound(SimpleName variable, Constant... constants) {
        return new Round(this, generateRound(variable, new NameExpr(constants[0].getVarName())), null);
    }
}
