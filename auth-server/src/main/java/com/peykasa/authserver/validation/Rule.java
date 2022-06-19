package com.peykasa.authserver.validation;

import java.util.List;

/**
 * @author Yaser(amin) Sadeghi
 */
@FunctionalInterface
public interface Rule<T> {
    List<String> validate(T t);
}
