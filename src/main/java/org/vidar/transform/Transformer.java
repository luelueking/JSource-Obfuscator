package org.vidar.transform;

import com.github.javaparser.ast.CompilationUnit;

public interface Transformer<T> {
    void transform(CompilationUnit cu,T arg);
}
