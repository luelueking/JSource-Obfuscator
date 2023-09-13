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

import com.github.javaparser.ast.expr.SimpleName;
import org.vidar.utils.NameUtils;


public class StringEntry {
    private final SimpleName varName;
    private String rawString;

    public StringEntry(String rawString) {
        this(new SimpleName(NameUtils.generateLocalVariableName()), rawString);
    }

    public StringEntry(SimpleName varName, String rawString) {
        this.varName = varName;
        this.rawString = rawString;
    }

    public SimpleName getVarName() {
        return varName;
    }

    public String getRawString() {
        return rawString;
    }

    public void setRawString(String rawString) {
        this.rawString = rawString;
    }
}
